package com.rateel.app.domain.source

import com.rateel.app.core.model.AppResult

/**
 * Contract for a future Rateel-controlled source configuration endpoint.
 * No backend is invented in milestone 2; the Android policy model is ready for overrides.
 */
data class SourceRuntimeOverride(
    val sourceId: String,
    val enabled: Boolean? = null,
    val streamingEnabled: Boolean? = null,
    val downloadEnabled: Boolean? = null,
    val offlinePlaybackEnabled: Boolean? = null,
    val recordingEnabled: Boolean? = null,
    val sharingEnabled: Boolean? = null,
    val disabledReason: String? = null,
)

interface RemoteSourceConfigDataSource {
    suspend fun fetchOverrides(): AppResult<List<SourceRuntimeOverride>>
}
