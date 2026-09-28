package com.rateel.app.domain.model

enum class SourceRightsStatus {
    VERIFIED_ALLOWED, STREAM_ONLY, REQUIRES_ATTRIBUTION, REQUIRES_BACKEND,
    REQUIRES_PERIODIC_SYNC, PENDING_VERIFICATION, RESTRICTED, DISABLED,
}

data class ContentCapabilities(
    val canStream: Boolean,
    val canDownload: Boolean,
    val canRecord: Boolean,
    val canKeepOffline: Boolean,
    val canShare: Boolean,
    val requiresAttribution: Boolean,
    val offlineRetentionDays: Int?,
    val rightsStatus: SourceRightsStatus,
)

data class ContentAsset(
    val sourceId: String,
    val assetHost: String?,
    val returnedBySource: String,
    val isLiveChannel: Boolean = false,
)

object ContentCapabilityResolver {
    fun resolve(source: ContentSource, asset: ContentAsset): ContentCapabilities {
        val knownHost = when (source.id) {
            SourceIds.MP3_QURAN_V3 ->
                asset.assetHost == "mp3quran.net" || asset.assetHost?.endsWith(".mp3quran.net") == true
            SourceIds.QURANGO_STREAMS ->
                asset.assetHost == "qurango.net" || asset.assetHost?.endsWith(".qurango.net") == true
            else -> false
        }
        val stream = SourceRightsPolicy.evaluate(source, RightsAction.STREAM).allowed
        val thirdPartyOrLive = !knownHost || asset.isLiveChannel
        val download = !thirdPartyOrLive && SourceRightsPolicy.evaluate(source, RightsAction.DOWNLOAD).allowed
        val record = !thirdPartyOrLive && SourceRightsPolicy.evaluate(source, RightsAction.RECORD).allowed
        val offline = !thirdPartyOrLive && SourceRightsPolicy.evaluate(source, RightsAction.OFFLINE_PLAYBACK).allowed
        val share = !thirdPartyOrLive && SourceRightsPolicy.evaluate(source, RightsAction.SHARE).allowed
        val status = when {
            !source.isEnabled -> SourceRightsStatus.DISABLED
            !source.isVerified -> SourceRightsStatus.PENDING_VERIFICATION
            source.id == SourceIds.QURAN_FOUNDATION -> SourceRightsStatus.REQUIRES_BACKEND
            thirdPartyOrLive -> SourceRightsStatus.STREAM_ONLY
            source.requiresPeriodicSync -> SourceRightsStatus.REQUIRES_PERIODIC_SYNC
            source.requiresAttribution -> SourceRightsStatus.REQUIRES_ATTRIBUTION
            stream -> SourceRightsStatus.VERIFIED_ALLOWED
            else -> SourceRightsStatus.RESTRICTED
        }
        return ContentCapabilities(
            canStream = stream,
            canDownload = download,
            canRecord = record,
            canKeepOffline = offline,
            canShare = share,
            requiresAttribution = source.requiresAttribution,
            offlineRetentionDays = source.maxOfflineRetentionDays,
            rightsStatus = status,
        )
    }
}
