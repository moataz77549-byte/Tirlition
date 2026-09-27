package com.rateel.app.playback

import com.rateel.app.domain.model.ContentSource
import com.rateel.app.domain.model.SourceRightsPolicy
import com.rateel.app.domain.model.RightsAction
import com.rateel.app.domain.model.StreamEndpoint

enum class RecordingMode {
    MANUAL,
    FIXED_DURATION,
}

data class RecordingRequest(
    val stationId: String,
    val source: ContentSource,
    val endpoint: StreamEndpoint,
    val mode: RecordingMode,
    val requestedDurationMs: Long? = null,
)

sealed interface RecordingCapability {
    data object Supported : RecordingCapability
    data class Unsupported(val reason: String) : RecordingCapability
}

/**
 * Future stream recorder boundary. Implementations record network stream bytes, never microphone audio.
 */
interface StreamRecorder {
    fun capability(endpoint: StreamEndpoint): RecordingCapability
    suspend fun start(request: RecordingRequest): Result<String>
    suspend fun stop(): Result<Unit>
}

object StreamRecordingPolicy {
    fun canRecord(source: ContentSource, endpoint: StreamEndpoint): RecordingCapability {
        val rights = SourceRightsPolicy.evaluate(source, RightsAction.RECORD)
        if (!rights.allowed) return RecordingCapability.Unsupported(rights.reason ?: "rights_denied")

        val supported = when (endpoint.format?.lowercase()) {
            "mp3", "aac", "aac+", "hls", "m3u8" -> true
            else -> false
        }
        return if (supported) RecordingCapability.Supported
        else RecordingCapability.Unsupported("unsupported_stream_format")
    }
}
