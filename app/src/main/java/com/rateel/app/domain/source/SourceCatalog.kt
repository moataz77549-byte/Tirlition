package com.rateel.app.domain.source

import com.rateel.app.domain.model.ContentSource
import com.rateel.app.domain.model.LicenseType
import com.rateel.app.domain.model.SourceIds
import com.rateel.app.domain.model.SourceType

private const val VERIFIED_2026_09_28 = 1_790_553_600_000L

object PlannedSourceCatalog {
    val mp3QuranV3 = ContentSource(
        id = SourceIds.MP3_QURAN_V3,
        name = "MP3Quran API v3",
        provider = "MP3Quran.net",
        type = SourceType.API,
        website = "https://www.mp3quran.net/",
        apiBaseUrl = "https://www.mp3quran.net/api/v3/",
        documentationUrl = "https://www.mp3quran.net/ar/api/2",
        termsUrl = "https://www.mp3quran.net/privacy-en.html",
        copyrightUrl = "https://www.mp3quran.net/eng/contact-us",
        attributionText = "MP3Quran.net",
        licenseType = LicenseType.PROVIDER_TERMS,
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
        notes = "Official MP3Quran pages permit copying site material and using site URLs. Recording, downloaded-file redistribution and commercial-use permissions remain disabled until explicitly verified.",
    )

    val qurangoStreams = ContentSource(
        id = SourceIds.QURANGO_STREAMS,
        name = "Qurango Streams",
        provider = "MP3Quran.net / Qurango.net",
        type = SourceType.CONTENT_PROVIDER,
        website = "https://qurango.net/",
        termsUrl = "https://www.mp3quran.net/privacy-en.html",
        copyrightUrl = "https://www.mp3quran.net/eng/contact-us",
        attributionText = "Qurango.net",
        licenseType = LicenseType.PROVIDER_TERMS,
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
        notes = "MP3Quran's published policy states that it also applies to Qurango.net. Public reachability is not treated as recording permission.",
    )

    val quranFoundation = ContentSource(
        id = SourceIds.QURAN_FOUNDATION,
        name = "Quran Foundation APIs",
        provider = "Quran Foundation",
        type = SourceType.API,
        website = "https://quran.foundation/",
        apiBaseUrl = "https://apis.quran.foundation/",
        documentationUrl = "https://api-docs.quran.com/",
        termsUrl = "https://api-docs.quran.com/legal/developer-terms/",
        copyrightUrl = "https://api-docs.quran.com/legal/developer-terms/",
        attributionText = "Quran Foundation",
        licenseType = LicenseType.PROVIDER_TERMS,
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
        disabledReason = "backend_proxy_and_content_scope_required",
        streamingEnabled = false,
        downloadEnabled = false,
        offlinePlaybackEnabled = false,
        cachingEnabled = false,
        offlineSyncEnabled = false,
        recordingEnabled = false,
        sharingEnabled = false,
        notes = "Content APIs require server-held credentials. Generic storage is limited to one week unless an explicit exception or documented Content Sync resource applies.",
    )

    val mp3QuranLiveTv = ContentSource(
        id = SourceIds.MP3_QURAN_LIVE_TV,
        name = "MP3Quran live channels",
        provider = "MP3Quran.net / channel broadcaster",
        type = SourceType.CONTENT_PROVIDER,
        website = "https://www.mp3quran.net/",
        apiBaseUrl = "https://www.mp3quran.net/api/v3/live-tv",
        documentationUrl = "https://www.mp3quran.net/ar/api/2",
        licenseType = LicenseType.UNKNOWN,
        allowStreaming = true,
        isOfficial = false,
        isVerified = false,
        isEnabled = false,
        streamingEnabled = false,
        disabledReason = "live_tv_asset_urls_unverified",
        lastTechnicalCheckAt = VERIFIED_2026_09_28,
        notes = "Audio from the Quran and Sunna live TV channels. Asset host and downstream rights require separate verification; streaming only.",
    )

    val all = listOf(mp3QuranV3, qurangoStreams, quranFoundation, mp3QuranLiveTv)
}
