package com.rateel.app.download

import android.content.Context
import android.net.Uri
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.rateel.app.data.local.DownloadDao
import com.rateel.app.data.local.DownloadEntity
import com.rateel.app.data.settings.AppSettings
import com.rateel.app.domain.model.ContentAsset
import com.rateel.app.domain.model.ContentCapabilityResolver
import com.rateel.app.domain.model.SurahAudio
import com.rateel.app.domain.repository.AudioRepository
import com.rateel.app.domain.repository.SourceRepository
import com.rateel.app.playback.PlaybackController
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first
import java.io.File
import java.net.URI

@Singleton
class RateelDownloadManager @Inject constructor(
    private val dao: DownloadDao, private val sources: SourceRepository,
    private val audio: AudioRepository, private val settings: AppSettings,
    private val storage: OfflineMediaStore, private val playback: PlaybackController,
    @ApplicationContext private val context: Context,
) {
    val downloads = dao.observeAll()
    private val work get() = WorkManager.getInstance(context)
    private fun workName(id: String) = "rateel-download-$id"
    suspend fun enqueue(track: SurahAudio, reciterId: String?): Result<String> = runCatching {
        val source = sources.getSource(track.sourceId) ?: error("SOURCE_UNKNOWN")
        val uri = URI(track.audioUrl)
        val rights = ContentCapabilityResolver.resolve(source,
            ContentAsset(track.sourceId, uri.host?.lowercase(), track.sourceId))
        check(track.downloadable && rights.canDownload && rights.canKeepOffline) { "RIGHTS_DENIED" }
        check(uri.scheme == "https" || uri.scheme == "http") { "INVALID_URL" }
        check(track.format?.lowercase() in listOf(null, "mp3", "aac", "m4a")) { "UNSUPPORTED_FORMAT" }
        check(storage.enough(track.fileSizeBytes)) { "NO_SPACE" }
        val existing = dao.byContent(track.id)
        if (existing?.status == "COMPLETED" && existing.localUri?.let { File(Uri.parse(it).path ?: "").isFile } == true) return@runCatching existing.id
        if (existing?.status in setOf("DOWNLOADING", "VERIFYING", "QUEUED", "WAITING_FOR_NETWORK")) return@runCatching existing!!.id
        val now = System.currentTimeMillis()
        val id = existing?.id ?: track.id
        dao.upsert(DownloadEntity(id, track.sourceId, track.audioUrl, null, "audio", reciterId,
            track.mushafId, track.surahNumber, track.fileSizeBytes, track.checksum, null,
            "${source.licenseType}:${rights.rightsStatus}:${source.attributionText.orEmpty()}", "QUEUED", 0,
            track.fileSizeBytes, now, contentId = track.id, expectedSize = track.fileSizeBytes,
            format = track.format, checksumAlgorithm = if (track.checksum?.matches(Regex("[0-9a-fA-F]{64}")) == true) "SHA-256" else null,
            createdAt = existing?.createdAt ?: now,
            expiresAt = rights.offlineRetentionDays?.let { now + it * 86_400_000L }))
        schedule(id)
        id
    }
    suspend fun enqueueBatch(tracks: List<SurahAudio>, reciterId: String?): List<Result<String>> {
        val distinct = tracks.distinctBy { it.id }
        val known = distinct.mapNotNull { it.fileSizeBytes }
        if (known.size == distinct.size && !storage.enough(known.sum())) return distinct.map { Result.failure(IllegalStateException("NO_SPACE")) }
        return distinct.map { enqueue(it, reciterId) }
    }
    suspend fun enqueueMushaf(mushafId: String, reciterId: String?): List<Result<String>> =
        enqueueBatch(audio.observeTracks(mushafId).first(), reciterId)
    suspend fun schedule(id: String) {
        val unmetered = settings.preferences.first().wifiOnlyDownloads
        val request = OneTimeWorkRequestBuilder<AudioDownloadWorker>()
            .setInputData(workDataOf("id" to id))
            .setConstraints(Constraints.Builder().setRequiredNetworkType(if (unmetered) NetworkType.UNMETERED else NetworkType.CONNECTED).build())
            .addTag("rateel-download").build()
        work.enqueueUniqueWork(workName(id), ExistingWorkPolicy.REPLACE, request)
    }
    suspend fun pause(id: String) { dao.status(id, "PAUSED", System.currentTimeMillis()); work.cancelUniqueWork(workName(id)) }
    suspend fun resume(id: String) { val row = dao.get(id) ?: return; if (row.status in setOf("PAUSED", "FAILED", "MISSING_FILE", "WAITING_FOR_NETWORK")) {
        dao.status(id, "QUEUED", System.currentTimeMillis()); schedule(id)
    } }
    suspend fun cancel(id: String) {
        val row = dao.get(id) ?: return
        dao.status(id, "CANCELLED", System.currentTimeMillis())
        work.cancelUniqueWork(workName(id))
        row.surahNumber?.let { n ->
            storage.part(storage.file(row.sourceId, row.reciterId.orEmpty(), row.mushafId.orEmpty(), n, row.format)).delete()
        }
    }
    suspend fun delete(id: String) { val row = dao.get(id) ?: return
        check(row.localUri == null || playback.state.value.currentItem?.localUri != row.localUri) { "PLAYBACK_BUSY" }
        cancel(id)
        row.localUri?.let { storage.ownedFile(it)?.delete() }
        row.surahNumber?.let { n -> storage.file(row.sourceId, row.reciterId.orEmpty(), row.mushafId.orEmpty(), n, row.format).let { storage.part(it).delete() } }
        dao.remove(id)
    }
    suspend fun deleteMushaf(id: String) {
        val rows = dao.byMushaf(id)
        check(rows.none { it.localUri != null && playback.state.value.currentItem?.localUri == it.localUri }) { "PLAYBACK_BUSY" }
        rows.forEach { delete(it.id) }
    }
    suspend fun localUri(contentId: String): String? {
        val row = dao.byContent(contentId) ?: return null
        if (row.status != "COMPLETED") return null
        val file = row.localUri?.let { storage.ownedFile(it) }
        if (file?.isFile != true || file.length() <= 0L) { dao.status(row.id, "MISSING_FILE", System.currentTimeMillis()); return null }
        if (row.expiresAt != null && row.expiresAt < System.currentTimeMillis()) { dao.status(row.id, "EXPIRED", System.currentTimeMillis()); return null }
        if (!storage.plausibleAudio(file, row.format, row.fileSizeBytes)) {
            dao.corrupt(row.id, System.currentTimeMillis()); return null
        }
        return row.localUri
    }
    suspend fun requeuePending() { dao.pending().forEach { schedule(it.id) } }
    suspend fun wifiOnly(value: Boolean) { settings.setWifiOnlyDownloads(value); requeuePending() }
    suspend fun storageBytes() = storage.usage()
    suspend fun freeBytes() = storage.availableBytes()
    suspend fun recordingsBytes() = storage.recordingsUsage()
}
