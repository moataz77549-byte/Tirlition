package com.rateel.app.domain.model

enum class SourceType { API, RADIO_DIRECTORY, OFFICIAL_BROADCASTER, CONTENT_PROVIDER, OTHER }

enum class LicenseType { UNKNOWN, PROVIDER_TERMS, WRITTEN_PERMISSION, OPEN_LICENSE, PUBLIC_DOMAIN, CUSTOM }

data class ContentSource(
    val id: String,
    val name: String,
    val provider: String,
    val type: SourceType,
    val website: String? = null,
    val apiBaseUrl: String? = null,
    val documentationUrl: String? = null,
    val termsUrl: String? = null,
    val copyrightUrl: String? = null,
    val attributionText: String? = null,
    val licenseType: LicenseType = LicenseType.UNKNOWN,
    val requiresAttribution: Boolean = false,
    val allowStreaming: Boolean = false,
    val allowDownload: Boolean = false,
    val allowOfflinePlayback: Boolean = false,
    val allowCaching: Boolean = false,
    val allowOfflineSync: Boolean = false,
    val allowRecording: Boolean = false,
    val allowSharing: Boolean = false,
    val allowCommercialUse: Boolean = false,
    val maxOfflineRetentionDays: Int? = null,
    val requiresPeriodicSync: Boolean = false,
    val isOfficial: Boolean = false,
    val isVerified: Boolean = false,
    val lastRightsCheckAt: Long? = null,
    val lastTechnicalCheckAt: Long? = null,
    val notes: String? = null,
    val isEnabled: Boolean = true,
    val streamingEnabled: Boolean = true,
    val downloadEnabled: Boolean = true,
    val offlinePlaybackEnabled: Boolean = true,
    val cachingEnabled: Boolean = true,
    val offlineSyncEnabled: Boolean = true,
    val recordingEnabled: Boolean = true,
    val sharingEnabled: Boolean = true,
    val disabledReason: String? = null,
)

enum class RightsAction { STREAM, DOWNLOAD, OFFLINE_PLAYBACK, CACHE, OFFLINE_SYNC, RECORD, SHARE, COMMERCIAL_USE }

data class RightsDecision(
    val allowed: Boolean,
    val attributionRequired: Boolean,
    val maxOfflineRetentionDays: Int? = null,
    val reason: String? = null,
)

object SourceRightsPolicy {
    fun evaluate(source: ContentSource, action: RightsAction): RightsDecision {
        if (!source.isEnabled) return denied(source, source.disabledReason ?: "source_disabled")
        if (!source.isVerified) return denied(source, "source_not_verified")

        val remotelyEnabled = when (action) {
            RightsAction.STREAM -> source.streamingEnabled
            RightsAction.DOWNLOAD -> source.downloadEnabled
            RightsAction.OFFLINE_PLAYBACK -> source.offlinePlaybackEnabled
            RightsAction.CACHE -> source.cachingEnabled
            RightsAction.OFFLINE_SYNC -> source.offlineSyncEnabled
            RightsAction.RECORD -> source.recordingEnabled
            RightsAction.SHARE -> source.sharingEnabled
            RightsAction.COMMERCIAL_USE -> true
        }
        if (!remotelyEnabled) return denied(source, "action_disabled_remotely")

        val rightsAllowed = when (action) {
            RightsAction.STREAM -> source.allowStreaming
            RightsAction.DOWNLOAD -> source.allowDownload
            RightsAction.OFFLINE_PLAYBACK -> source.allowOfflinePlayback
            RightsAction.CACHE -> source.allowCaching
            RightsAction.OFFLINE_SYNC -> source.allowOfflineSync
            RightsAction.RECORD -> source.allowRecording
            RightsAction.SHARE -> source.allowSharing
            RightsAction.COMMERCIAL_USE -> source.allowCommercialUse
        }

        return if (rightsAllowed) {
            RightsDecision(true, source.requiresAttribution, source.maxOfflineRetentionDays)
        } else {
            denied(source, "action_not_permitted")
        }
    }

    private fun denied(source: ContentSource, reason: String) =
        RightsDecision(false, source.requiresAttribution, source.maxOfflineRetentionDays, reason)
}

object SourceIds {
    const val MP3_QURAN_V3 = "mp3quran-v3"
    const val QURANGO_STREAMS = "qurango-streams"
    const val QURAN_FOUNDATION = "quran-foundation"
}
