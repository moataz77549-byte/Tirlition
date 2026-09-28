package com.rateel.app.domain.model

enum class SourceRightsStatus {
    VERIFIED_ALLOWED,
    STREAM_ONLY,
    REQUIRES_ATTRIBUTION,
    REQUIRES_BACKEND,
    REQUIRES_PERIODIC_SYNC,
    PENDING_VERIFICATION,
    RESTRICTED,
    DISABLED,
}

enum class AssetRightsStatus {
    INHERIT_SOURCE,
    VERIFIED_ALLOWED,
    STREAM_ONLY,
    PENDING_VERIFICATION,
    RESTRICTED,
}

enum class ContentType {
    RADIO_STREAM,
    LIVE_CHANNEL_AUDIO,
    SURAH_AUDIO,
    CHAPTER_AUDIO,
    LOCAL_RECORDING,
    DOWNLOADED_SURAH,
}

data class ContentCapabilities(
    val canStream: Boolean,
    val canDownload: Boolean,
    val canRecord: Boolean,
    val canKeepOffline: Boolean,
    val canShare: Boolean,
    val requiresAttribution: Boolean,
    val maxOfflineRetentionDays: Int?,
)

object ContentCapabilityResolver {
    fun resolve(
        source: ContentSource,
        contentType: ContentType,
        assetRightsStatus: AssetRightsStatus = AssetRightsStatus.INHERIT_SOURCE,
    ): ContentCapabilities {
        val stream = SourceRightsPolicy.evaluate(source, RightsAction.STREAM).allowed
        val download = SourceRightsPolicy.evaluate(source, RightsAction.DOWNLOAD).allowed
        val offline = SourceRightsPolicy.evaluate(source, RightsAction.OFFLINE_PLAYBACK).allowed
        val record = SourceRightsPolicy.evaluate(source, RightsAction.RECORD).allowed
        val share = SourceRightsPolicy.evaluate(source, RightsAction.SHARE).allowed

        val assetAllowsOnlyStreaming =
            assetRightsStatus == AssetRightsStatus.STREAM_ONLY ||
                assetRightsStatus == AssetRightsStatus.PENDING_VERIFICATION
        val assetRestricted = assetRightsStatus == AssetRightsStatus.RESTRICTED

        return ContentCapabilities(
            canStream = stream && !assetRestricted,
            canDownload = download && !assetAllowsOnlyStreaming && !assetRestricted &&
                contentType != ContentType.RADIO_STREAM &&
                contentType != ContentType.LIVE_CHANNEL_AUDIO,
            canRecord = record && !assetAllowsOnlyStreaming && !assetRestricted &&
                contentType == ContentType.RADIO_STREAM,
            canKeepOffline = offline && !assetAllowsOnlyStreaming && !assetRestricted,
            canShare = share && !assetAllowsOnlyStreaming && !assetRestricted,
            requiresAttribution = source.requiresAttribution,
            maxOfflineRetentionDays = source.maxOfflineRetentionDays,
        )
    }
}
