package com.rateel.app.domain.source

import com.rateel.app.domain.model.ContentSource
import com.rateel.app.domain.model.LicenseType
import com.rateel.app.domain.model.SourceIds
import com.rateel.app.domain.model.SourceType

/**
 * Planned providers are intentionally disabled until their rights record and
 * production access are both verified. Unknown permissions always stay false.
 */
object PlannedSourceCatalog {
    val mp3QuranV3 = ContentSource(
        id = SourceIds.MP3_QURAN_V3,
        name = "MP3Quran API v3",
        provider = "MP3Quran.net",
        type = SourceType.API,
        website = "https://www.mp3quran.net/",
        apiBaseUrl = "https://www.mp3quran.net/api/v3/",
        documentationUrl = "https://www.mp3quran.net/ar/api/2",
        licenseType = LicenseType.UNKNOWN,
        isOfficial = true,
        isVerified = false,
        isEnabled = false,
        disabledReason = "rights_review_required",
        streamingEnabled = false,
        downloadEnabled = false,
        offlinePlaybackEnabled = false,
        recordingEnabled = false,
        sharingEnabled = false,
        notes = "Official API endpoints are technically documented. Content-use rights must be verified before production enablement.",
    )

    val qurangoStreams = ContentSource(
        id = SourceIds.QURANGO_STREAMS,
        name = "Qurango Streams",
        provider = "Qurango.net",
        type = SourceType.CONTENT_PROVIDER,
        licenseType = LicenseType.UNKNOWN,
        isVerified = false,
        isEnabled = false,
        disabledReason = "rights_review_required",
        streamingEnabled = false,
        downloadEnabled = false,
        offlinePlaybackEnabled = false,
        recordingEnabled = false,
        sharingEnabled = false,
        notes = "MP3Quran v3 radio responses currently reference Qurango stream URLs. Direct rights remain unverified.",
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
        attributionText = "Quran data provided by Quran Foundation.",
        licenseType = LicenseType.PROVIDER_TERMS,
        requiresAttribution = true,
        allowStreaming = true,
        allowCommercialUse = true,
        maxOfflineRetentionDays = 7,
        requiresPeriodicSync = true,
        isOfficial = true,
        isVerified = true,
        lastRightsCheckAt = 1_790_553_600_000L,
        lastTechnicalCheckAt = 1_790_553_600_000L,
        isEnabled = false,
        disabledReason = "production_access_required",
        streamingEnabled = false,
        downloadEnabled = false,
        offlinePlaybackEnabled = false,
        recordingEnabled = false,
        sharingEnabled = false,
        notes = "Developer terms reviewed 2026-09-28. Mobile client secrets are forbidden; content-specific licensing and production approval still apply.",
    )

    val all: List<ContentSource> = listOf(mp3QuranV3, qurangoStreams, quranFoundation)
}
