package com.rateel.app.domain.source

import com.rateel.app.domain.model.*

private const val VERIFIED_2026_09_28 = 1_790_553_600_000L

object SourceRegistry {
    val mp3Quran = ContentSource(
        id = SourceIds.MP3_QURAN_V3,
        name = "MP3Quran API v3",
        provider = "MP3Quran.net",
        type = SourceType.API,
        website = "https://www.mp3quran.net/",
        apiBaseUrl = "https://www.mp3quran.net/api/v3/",
        documentationUrl = "https://www.mp3quran.net/eng/api",
        termsUrl = "https://www.mp3quran.net/privacy-en.html",
        copyrightUrl = "https://www.mp3quran.net/privacy-en.html",
        attributionText = "MP3Quran.net",
        licenseType = LicenseType.PROVIDER_TERMS,
        rightsStatus = SourceRightsStatus.VERIFIED_ALLOWED,
        allowStreaming = true,
        allowDownload = true,
        allowOfflinePlayback = true,
        allowCaching = true,
        allowOfflineSync = true,
        allowRecording = false,
        allowSharing = false,
        allowCommercialUse = false,
        isOfficial = true,
        isVerified = true,
        lastRightsCheckAt = VERIFIED_2026_09_28,
        lastTechnicalCheckAt = VERIFIED_2026_09_28,
        priority = 10,
        fallbackPriority = 10,
        health = SourceHealth.UNKNOWN,
        notes = "Official policy permits visitors/developers to copy site material or use site links. Recording and unrestricted redistribution remain disabled in Rateel.",
    )

    val qurango = ContentSource(
        id = SourceIds.QURANGO_STREAMS,
        name = "Qurango Streams",
        provider = "MP3Quran.net / Qurango.net",
        type = SourceType.CONTENT_PROVIDER,
        website = "https://qurango.net/",
        termsUrl = "https://www.mp3quran.net/privacy-en.html",
        copyrightUrl = "https://www.mp3quran.net/privacy-en.html",
        attributionText = "Qurango.net",
        licenseType = LicenseType.PROVIDER_TERMS,
        rightsStatus = SourceRightsStatus.STREAM_ONLY,
        allowStreaming = true,
        allowRecording = false,
        allowDownload = false,
        allowOfflinePlayback = false,
        allowCaching = false,
        allowOfflineSync = false,
        allowSharing = false,
        allowCommercialUse = false,
        isOfficial = true,
        isVerified = true,
        lastRightsCheckAt = VERIFIED_2026_09_28,
        lastTechnicalCheckAt = VERIFIED_2026_09_28,
        priority = 20,
        fallbackPriority = 20,
        notes = "MP3Quran states that its published policy applies to Qurango.net; live-stream recording/download stays disabled.",
    )

    val mp3QuranLiveTv = ContentSource(
        id = SourceIds.MP3_QURAN_LIVE_TV,
        name = "MP3Quran Live TV",
        provider = "MP3Quran.net",
        type = SourceType.OFFICIAL_BROADCASTER,
        website = "https://www.mp3quran.net/",
        apiBaseUrl = "https://www.mp3quran.net/api/v3/live-tv",
        documentationUrl = "https://www.mp3quran.net/eng/api",
        termsUrl = "https://www.mp3quran.net/privacy-en.html",
        attributionText = "MP3Quran.net",
        licenseType = LicenseType.PROVIDER_TERMS,
        rightsStatus = SourceRightsStatus.STREAM_ONLY,
        allowStreaming = true,
        allowDownload = false,
        allowOfflinePlayback = false,
        allowRecording = false,
        allowSharing = false,
        allowCommercialUse = false,
        isOfficial = true,
        isVerified = true,
        lastRightsCheckAt = VERIFIED_2026_09_28,
        lastTechnicalCheckAt = VERIFIED_2026_09_28,
        priority = 15,
        fallbackPriority = 15,
        notes = "Audio-first access to live-tv metadata. Returned asset hosts keep their own asset-rights status; recording/download are disabled.",
    )

    val quranFoundation = ContentSource(
        id = SourceIds.QURAN_FOUNDATION,
        name = "Quran Foundation APIs",
        provider = "Quran Foundation",
        type = SourceType.API,
        website = "https://quran.foundation/",
        apiBaseUrl = "https://apis.quran.foundation/",
        documentationUrl = "https://api-docs.quran.foundation/",
        termsUrl = "https://api-docs.quran.foundation/legal/developer-terms/",
        copyrightUrl = "https://api-docs.quran.foundation/legal/developer-terms/",
        attributionText = "Quran data provided by Quran Foundation.",
        licenseType = LicenseType.PROVIDER_TERMS,
        rightsStatus = SourceRightsStatus.REQUIRES_BACKEND,
        requiresAttribution = true,
        allowStreaming = true,
        allowDownload = false,
        allowOfflinePlayback = true,
        allowCaching = true,
        allowOfflineSync = true,
        allowRecording = false,
        allowSharing = false,
        allowCommercialUse = true,
        maxOfflineRetentionDays = 7,
        requiresPeriodicSync = true,
        isOfficial = true,
        isVerified = true,
        lastRightsCheckAt = VERIFIED_2026_09_28,
        lastTechnicalCheckAt = VERIFIED_2026_09_28,
        isEnabled = false,
        streamingEnabled = false,
        downloadEnabled = false,
        offlinePlaybackEnabled = false,
        cachingEnabled = false,
        offlineSyncEnabled = false,
        recordingEnabled = false,
        sharingEnabled = false,
        priority = 50,
        fallbackPriority = 50,
        disabledReason = "backend_proxy_and_content_scope_required",
        notes = "Confidential Content API credentials stay on a Rateel backend. Generic cached QF Content is limited to one week unless an explicit exception or Content Sync applies.",
    )

    val all: List<ContentSource> = listOf(mp3Quran, qurango, mp3QuranLiveTv, quranFoundation)
    fun byId(id: String): ContentSource? = all.firstOrNull { it.id == id }
}

/** Backward-compatible name retained for milestone-1 callers. */
object PlannedSourceCatalog {
    val mp3QuranV3 get() = SourceRegistry.mp3Quran
    val qurangoStreams get() = SourceRegistry.qurango
    val quranFoundation get() = SourceRegistry.quranFoundation
    val all get() = SourceRegistry.all
}


val ContentSource.baseHost: String?
    get() = runCatching {
        java.net.URI(apiBaseUrl ?: website ?: return null).host?.lowercase()
    }.getOrNull()
