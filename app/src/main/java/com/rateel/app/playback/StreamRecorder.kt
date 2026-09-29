package com.rateel.app.playback

import com.rateel.app.domain.model.ContentAsset
import com.rateel.app.domain.model.ContentCapabilityResolver
import com.rateel.app.domain.model.ContentSource
import com.rateel.app.domain.model.StreamEndpoint
import java.net.URI
import kotlinx.coroutines.flow.StateFlow

enum class RecordingMode { MANUAL, FIXED_DURATION }
data class RecordingRequest(
    val stationId: String,
    val stationName: String,
    val endpoint: StreamEndpoint,
    val mode: RecordingMode,
    val requestedDurationMs: Long? = null,
)
sealed interface RecordingCapability {
    data object Supported : RecordingCapability
    data class Unsupported(val reason: String) : RecordingCapability
}
enum class RecordingStatus { IDLE, PREPARING, RECORDING, FINALIZING, COMPLETED, FAILED, CANCELLED }
data class RecordingState(
    val status: RecordingStatus = RecordingStatus.IDLE,
    val stationId: String? = null,
    val elapsedMs: Long = 0,
    val targetMs: Long? = null,
    val bytesWritten: Long = 0,
    val outputId: String? = null,
    val error: String? = null,
)
interface StreamRecorder {
    val state: StateFlow<RecordingState>
    suspend fun start(request: RecordingRequest): Result<String>
    suspend fun stop(): Result<Unit>
    suspend fun cancel(): Result<Unit>
}
object RecordingDurations { val supportedMinutes = listOf(5, 10, 15, 30) }
object StreamRecordingPolicy {
    fun canRecord(source: ContentSource, endpoint: StreamEndpoint): RecordingCapability {
        val host = runCatching { URI(endpoint.url).host?.lowercase() }.getOrNull()
        val rights = ContentCapabilityResolver.resolve(source,
            ContentAsset(endpoint.sourceId, host, source.id))
        if (!rights.canRecord) return RecordingCapability.Unsupported("rights_denied")
        if (!endpoint.url.startsWith("https://")) return RecordingCapability.Unsupported("https_required")
        return when (endpoint.format?.lowercase()) {
            "mp3", "aac", "aac+" -> RecordingCapability.Supported
            else -> RecordingCapability.Unsupported("unsupported_stream_format")
        }
    }
}
object RecordingFileNames {
    fun create(stationId: String, timestamp: Long, extension: String): String {
        val safe = stationId.replace(Regex("[^a-zA-Z0-9_-]"), "_").take(60)
        require(extension == "mp3" || extension == "aac")
        return "rateel_recording_${safe}_${timestamp}.$extension"
    }
}
