package com.rateel.app.download

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.hilt.work.HiltWorker
import com.rateel.app.R
import com.rateel.app.data.local.AudioTrackDao
import com.rateel.app.data.local.DownloadDao
import com.rateel.app.domain.model.ContentAsset
import com.rateel.app.domain.model.ContentCapabilityResolver
import com.rateel.app.domain.repository.SourceRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.io.File
import java.io.FileOutputStream
import java.net.URI
import java.security.MessageDigest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import okhttp3.OkHttpClient
import okhttp3.Request

@HiltWorker
class AudioDownloadWorker @AssistedInject constructor(
    @Assisted context: Context, @Assisted params: WorkerParameters,
    private val dao: DownloadDao, private val tracks: AudioTrackDao,
    private val sources: SourceRepository, private val store: OfflineMediaStore,
    private val client: OkHttpClient,
) : CoroutineWorker(context, params) {
    companion object { private val transfers = Semaphore(2); private const val CHANNEL = "rateel_downloads" }
    private val notifications = context.getSystemService(NotificationManager::class.java)
    override suspend fun doWork(): Result = transfers.withPermit {
        val id = inputData.getString("id") ?: return@withPermit Result.failure()
        var row = dao.get(id) ?: return@withPermit Result.failure()
        if (row.status in setOf("PAUSED", "CANCELLED", "COMPLETED")) return@withPermit Result.success()
        val source = sources.getSource(row.sourceId) ?: return@withPermit fail(id, "SOURCE_UNKNOWN")
        val track = row.mushafId?.let { tracks.getById(row.contentId) } ?: return@withPermit fail(id, "TRACK_MISSING")
        val url = track.audioUrl // refresh from cached canonical track, never trust an old redirect
        val host = runCatching { URI(url).host?.lowercase() }.getOrNull()
        val rights = ContentCapabilityResolver.resolve(source, ContentAsset(row.sourceId, host, row.sourceId))
        if (!rights.canDownload || !rights.canKeepOffline || !track.downloadable) return@withPermit fail(id, "RIGHTS_DENIED")
        if (!url.startsWith("https://")) return@withPermit fail(id, "INVALID_URL")
        val target = store.file(row.sourceId, row.reciterId.orEmpty(), row.mushafId.orEmpty(), row.surahNumber ?: return@withPermit fail(id, "TRACK_MISSING"), row.format)
        val part = store.part(target)
        try {
            if (Build.VERSION.SDK_INT >= 26) notifications.createNotificationChannel(NotificationChannel(CHANNEL, applicationContext.getString(R.string.downloads), NotificationManager.IMPORTANCE_LOW))
            setForeground(foreground(row.id, track.surahNameArabic, 0))
            dao.status(id, "DOWNLOADING", System.currentTimeMillis())
            val previous = if (part.exists()) part.length() else 0L
            val request = Request.Builder().url(url).header("Accept-Encoding", "identity")
                .apply { if (previous > 0) {
                    header("Range", "bytes=$previous-")
                    row.etag?.let { header("If-Range", it) } ?: row.lastModified?.let { header("If-Range", it) }
                } }.build()
            client.newBuilder().readTimeout(90, java.util.concurrent.TimeUnit.SECONDS).build().newCall(request).execute().use { response ->
                if (response.code == 416) { part.delete(); return@withPermit fail(id, "RANGE_REJECTED") }
                if (!response.isSuccessful) return@withPermit fail(id, "HTTP_${response.code}")
                val finalHost = response.request.url.host.lowercase()
                if (row.sourceId == com.rateel.app.domain.model.SourceIds.MP3_QURAN_V3 &&
                    finalHost != "mp3quran.net" && !finalHost.endsWith(".mp3quran.net"))
                    return@withPermit fail(id, "UNTRUSTED_REDIRECT")
                val append = previous > 0 && response.code == 206 && response.header("Content-Range")?.startsWith("bytes $previous-") == true &&
                    (row.etag == null || response.header("ETag") == row.etag) &&
                    (row.lastModified == null || response.header("Last-Modified") == row.lastModified)
                if (response.code == 206 && !append) {
                    part.delete()
                    return@withPermit fail(id, "RANGE_MISMATCH")
                }
                val length = response.body?.contentLength()?.takeIf { it >= 0 }
                val total = length?.plus(if (append) previous else 0L) ?: row.expectedSize
                if (row.expectedSize != null && total != null && row.expectedSize != total)
                    return@withPermit fail(id, "SIZE_CHANGED")
                if (!store.enough(length)) return@withPermit fail(id, "NO_SPACE")
                if (dao.get(id)?.status != "DOWNLOADING") return@withPermit Result.success()
                row = row.copy(etag = response.header("ETag"), lastModified = response.header("Last-Modified"),
                    startedAt = System.currentTimeMillis(), totalBytes = total)
                row = row.copy(status = "DOWNLOADING")
                dao.upsert(row)
                val body = response.body ?: return@withPermit fail(id, "EMPTY_BODY")
                var bytes = if (append) previous else 0L
                var lastUpdate = 0L
                FileOutputStream(part, append).use { output ->
                    body.byteStream().use { input ->
                        val buffer = ByteArray(64 * 1024)
                        while (true) {
                            if (isStopped) throw CancellationException("stopped")
                            val count = input.read(buffer)
                            if (count < 0) break
                            output.write(buffer, 0, count)
                            bytes += count
                            val now = SystemClock.elapsedRealtime()
                            if (now - lastUpdate > 800) {
                                lastUpdate = now
                                dao.progressIfDownloading(id, bytes, total, System.currentTimeMillis())
                                setForeground(foreground(id, track.surahNameArabic, if (total != null && total > 0) (bytes * 100 / total).toInt().coerceIn(0, 99) else 0))
                            }
                        }
                    }
                    output.fd.sync()
                }
                if (dao.get(id)?.status != "DOWNLOADING") return@withPermit Result.success()
                dao.status(id, "VERIFYING", System.currentTimeMillis())
                if (part.length() <= 0 || (total != null && part.length() != total) ||
                    (row.checksumAlgorithm == "SHA-256" && row.checksum != null && !sha256(part).equals(row.checksum, true)) ||
                    !plausibleAudio(part, row.format)) {
                    part.delete(); return@withPermit fail(id, "INTEGRITY_CHECK_FAILED")
                }
                if (dao.get(id)?.status != "VERIFYING") return@withPermit Result.success()
                if (!part.renameTo(target)) return@withPermit fail(id, "FINALIZE_FAILED")
                val now = System.currentTimeMillis()
                dao.upsert(row.copy(localUri = Uri.fromFile(target).toString(), status = "COMPLETED",
                    bytesDownloaded = target.length(), totalBytes = target.length(), fileSizeBytes = target.length(),
                    updatedAt = now, completedAt = now, lastVerifiedAt = now, failureReason = null))
                Result.success()
            }
        } catch (e: CancellationException) { throw e }
        catch (e: Exception) { fail(id, if (e.message?.contains("space", true) == true) "NO_SPACE" else "TRANSFER_FAILED") }
    }
    private suspend fun fail(id: String, reason: String): Result {
        val row = dao.get(id) ?: return Result.failure()
        if (row.status !in setOf("PAUSED", "CANCELLED")) dao.upsert(row.copy(status = "FAILED", failureReason = reason,
            updatedAt = System.currentTimeMillis(), retryCount = row.retryCount + 1))
        return Result.failure()
    }
    private fun foreground(id: String, title: String, percent: Int): ForegroundInfo {
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL)
            .setSmallIcon(android.R.drawable.stat_sys_download).setContentTitle(title)
            .setContentText(applicationContext.getString(R.string.download_progress, percent))
            .setProgress(100, percent, percent == 0).setOngoing(true).build()
        return ForegroundInfo(id.hashCode() and Int.MAX_VALUE, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
    }
    private fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input -> val bytes = ByteArray(65536); while (true) { val n = input.read(bytes); if (n < 0) break; digest.update(bytes, 0, n) } }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
    private fun plausibleAudio(file: File, format: String?): Boolean {
        if (file.length() < 128) return false
        val header = ByteArray(12)
        file.inputStream().use { it.read(header) }
        return when (format?.lowercase()) {
            "aac", "m4a" -> (header[0].toInt() and 255 == 0xff && header[1].toInt() and 0xf0 == 0xf0) || String(header, 4, 4) == "ftyp"
            else -> String(header, 0, 3) == "ID3" || (header[0].toInt() and 255 == 0xff && header[1].toInt() and 0xe0 == 0xe0)
        }
    }
}
