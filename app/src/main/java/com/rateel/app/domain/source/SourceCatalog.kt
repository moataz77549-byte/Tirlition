package com.rateel.app.domain.source

import com.rateel.app.domain.model.ContentSource
import com.rateel.app.domain.model.LicenseType
import com.rateel.app.domain.model.SourceIds
import com.rateel.app.domain.model.SourceType

/**
 * Planned providers are intentionally non-production until their rights record is verified.
 * All permissions default to false, preventing accidental exposure of download/record/share actions.
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
        notes = "Technical API availability verified; production rights must be verified before enabling content.",
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
        maxOfflineRetentionDays = 7,
        requiresPeriodicSync = true,
        isOfficial = true,
        isVerified = false,
        notes = "Enable individual capabilities only after application approval and content-specific license review.",
    )

    val all: List<ContentSource> = listOf(mp3QuranV3, quranFoundation)
}
