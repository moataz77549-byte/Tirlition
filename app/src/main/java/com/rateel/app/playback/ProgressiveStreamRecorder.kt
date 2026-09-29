package com.rateel.app.playback

import android.content.Context
import android.os.StatFs
import com.rateel.app.data.local.LocalRecordingEntity
import com.rateel.app.data.local.RecordingDao
import com.rateel.app.domain.repository.SourceRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.IOException
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request

/** Progressive bytes only; no microphone, HLS concatenation, or protected stream capture. */
@Singleton
class ProgressiveStreamRecorder @Inject constructor(
    @ApplicationContext private val context: Context,
    private val client: OkHttpClient,
    private val sources: SourceRepository,
    private val dao: RecordingDao,
) : StreamRecorder {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mutex = Mutex()
    private val mutable = MutableStateFlow(RecordingState())
    override val state: StateFlow<RecordingState> = mutable
    @Volatile private var call: Call? = null
    @Volatile private var finish = false
    @Volatile private var discard = false
    private var job: Job? = null

    override suspend fun start(request: RecordingRequest): Result<String> = mutex.withLock {
        if (job?.isActive == true) return@withLock Result.failure(IllegalStateException("recording_already_active"))
        // Read the current rights record. Never trust capabilities or source snapshots supplied by UI.
        val source = sources.getSource(request.endpoint.sourceId)
            ?: return@withLock Result.failure(IllegalStateException("source_missing"))
        if (StreamRecordingPolicy.canRecord(source, request.endpoint) !is RecordingCapability.Supported)
            return@withLock Result.failure(IllegalStateException("recording_not_permitted"))
        if (request.mode == RecordingMode.FIXED_DURATION &&
            request.requestedDurationMs !in RecordingDurations.supportedMinutes.map { it * 60_000L })
            return@withLock Result.failure(IllegalArgumentException("unsupported_duration"))
        val directory = File(context.filesDir, "recordings").apply { mkdirs() }
        if (StatFs(directory.path).availableBytes < 25L * 1024 * 1024)
            return@withLock Result.failure(IOException("low_storage"))
        val extension = if (request.endpoint.format == "mp3") "mp3" else "aac"
        val startedAt = System.currentTimeMillis()
        val filename = RecordingFileNames.create(request.stationId, startedAt, extension)
        val temp = File(directory, "$filename.part")
        val final = File(directory, filename)
        val id = UUID.randomUUID().toString()
        finish = false; discard = false
        mutable.value = RecordingState(RecordingStatus.PREPARING, request.stationId,
            targetMs = request.requestedDurationMs)
        job = scope.launch {
            try {
                val networkCall = client.newCall(Request.Builder().url(request.endpoint.url)
                    .header("Icy-MetaData", "0").build())
                call = networkCall
                try {
                    networkCall.execute().use { response ->
                        if (!response.isSuccessful) throw IOException("stream_http_${response.code}")
                        val contentType = response.header("Content-Type").orEmpty().lowercase()
                        if (contentType.contains("html") || contentType.contains("mpegurl"))
                            throw IOException("unsupported_stream_response")
                        val input = response.body?.byteStream() ?: throw IOException("empty_stream")
                        val started = android.os.SystemClock.elapsedRealtime()
                        mutable.value = mutable.value.copy(status = RecordingStatus.RECORDING)
                        temp.outputStream().buffered().use { output ->
                            val buffer = ByteArray(16 * 1024)
                            while (!finish && !discard) {
                                val count = input.read(buffer)
                                if (count < 0) break
                                output.write(buffer, 0, count)
                                val elapsed = android.os.SystemClock.elapsedRealtime() - started
                                mutable.value = mutable.value.copy(elapsedMs = elapsed,
                                    bytesWritten = mutable.value.bytesWritten + count)
                                if (request.requestedDurationMs != null && elapsed >= request.requestedDurationMs) break
                                if (StatFs(directory.path).availableBytes < 5L * 1024 * 1024)
                                    throw IOException("low_storage")
                            }
                        }
                    }
                } catch (error: IOException) {
                    if (!finish && !discard) throw error
                }
                if (discard) {
                    temp.delete()
                    mutable.value = mutable.value.copy(status = RecordingStatus.CANCELLED)
                    return@launch
                }
                mutable.value = mutable.value.copy(status = RecordingStatus.FINALIZING)
                if (temp.length() < 1024) throw IOException("recording_too_short")
                if (!temp.renameTo(final)) throw IOException("file_finalize_failed")
                try {
                    dao.insert(LocalRecordingEntity(id = id, stationId = request.stationId,
                        sourceId = source.id, stationName = request.stationName, title = request.stationName,
                        filePath = final.absolutePath,
                        mimeType = if (extension == "mp3") "audio/mpeg" else "audio/aac",
                        durationMs = mutable.value.elapsedMs, fileSizeBytes = final.length(),
                        startedAt = startedAt, finishedAt = System.currentTimeMillis(),
                        recordingMode = request.mode.name, requestedDurationMs = request.requestedDurationMs,
                        artwork = null, sourceAttribution = source.attributionText, createdAt = startedAt,
                        rightsSnapshot = "source=${source.id};record=${source.allowRecording};verified=${source.isVerified}",
                        codec = extension))
                } catch (error: Exception) { final.delete(); throw error }
                mutable.value = mutable.value.copy(status = RecordingStatus.COMPLETED, outputId = id)
            } catch (error: Exception) {
                temp.delete()
                mutable.value = mutable.value.copy(status = if (discard) RecordingStatus.CANCELLED else RecordingStatus.FAILED,
                    error = if (discard) null else error.message)
            } finally { call = null }
        }
        Result.success(id)
    }

    override suspend fun stop(): Result<Unit> {
        if (job?.isActive != true) return Result.failure(IllegalStateException("no_recording"))
        finish = true; call?.cancel(); job?.join()
        return if (state.value.status == RecordingStatus.COMPLETED) Result.success(Unit)
            else Result.failure(IOException(state.value.error ?: "recording_failed"))
    }
    override suspend fun cancel(): Result<Unit> {
        discard = true; call?.cancel(); job?.join()
        return Result.success(Unit)
    }
}
