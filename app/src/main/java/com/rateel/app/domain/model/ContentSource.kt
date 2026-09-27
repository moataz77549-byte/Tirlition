package com.rateel.app.domain.model

enum class SourceType {
    API,
    RADIO_DIRECTORY,
    OFFICIAL_BROADCASTER,
    CONTENT_PROVIDER,
    OTHER,
}

enum class LicenseType {
    UNKNOWN,
    PROVIDER_TERMS,
    WRITTEN_PERMISSION,
    OPEN_LICENSE,
    PUBLIC_DOMAIN,
    CUSTOM,
}

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
)

enum class RightsAction {
    STREAM,
    DOWNLOAD,
    OFFLINE_PLAYBACK,
    RECORD,
    SHARE,
    COMMERCIAL_USE,
}

data class RightsDecision(
    val allowed: Boolean,
    val attributionRequired: Boolean,
    val maxOfflineRetentionDays: Int? = null,
    val reason: String? = null,
)

object SourceRightsPolicy {
    fun evaluate(
        source: ContentSource,
        action: RightsAction,
    ): RightsDecision {
        if (!source.isVerified) {
            return RightsDecision(
                allowed = false,
                attributionRequired = source.requiresAttribution,
                maxOfflineRetentionDays = source.maxOfflineRetentionDays,
                reason = "source_not_verified",
            )
        }

        val allowed = when (action) {
            RightsAction.STREAM -> source.allowStreaming
            RightsAction.DOWNLOAD -> source.allowDownload
            RightsAction.OFFLINE_PLAYBACK ->
                source.allowDownload && source.allowOfflinePlayback
            RightsAction.RECORD -> source.allowRecording
            RightsAction.SHARE -> source.allowSharing
            RightsAction.COMMERCIAL_USE -> source.allowCommercialUse
        }

        return RightsDecision(
            allowed = allowed,
            attributionRequired = source.requiresAttribution,
            maxOfflineRetentionDays = source.maxOfflineRetentionDays,
            reason = if (allowed) null else "action_not_permitted",
        )
    }
}

object SourceIds {
    const val MP3_QURAN_V3 = "mp3quran-v3"
    const val QURAN_FOUNDATION = "quran-foundation"
}
