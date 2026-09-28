package com.rateel.app.domain.model

enum class StreamHealth { UNKNOWN, ONLINE, OFFLINE, DEGRADED, UNSUPPORTED, BLOCKED }
enum class SourceHealth { HEALTHY, DEGRADED, OFFLINE, RATE_LIMITED, AUTH_ERROR, UNKNOWN }
enum class CategoryOrigin { SOURCE, DERIVED, UNKNOWN }

data class StreamEndpoint(
    val sourceId: String,
    val url: String,
    val format: String? = null,
    val bitrateKbps: Int? = null,
    val primary: Boolean = false,
    val providerEndpointId: String? = null,
    val returnedBySourceId: String? = null,
    val originalUrl: String = url,
    val resolvedUrl: String? = null,
    val assetHost: String? = null,
    val resolvedHost: String? = null,
    val lastResolvedAt: Long? = null,
    val health: StreamHealth = StreamHealth.UNKNOWN,
    val assetRightsStatus: AssetRightsStatus = AssetRightsStatus.INHERIT_SOURCE,
)

data class RadioStation(
    val id: String,
    val sourceId: String,
    val canonicalKey: String,
    val nameArabic: String,
    val nameEnglish: String? = null,
    val description: String? = null,
    val streams: List<StreamEndpoint>,
    val logoUrl: String? = null,
    val country: String? = null,
    val language: String = "ar",
    val category: String? = null,
    val categoryOrigin: CategoryOrigin = CategoryOrigin.UNKNOWN,
    val website: String? = null,
    val isActive: Boolean = true,
    val isFeatured: Boolean = false,
    val isVerified: Boolean = false,
    val health: StreamHealth = StreamHealth.UNKNOWN,
    val sourceHealth: SourceHealth = SourceHealth.UNKNOWN,
    val tags: List<String> = emptyList(),
    val createdAt: Long? = null,
    val updatedAt: Long? = null,
)

data class Reciter(
    val id: String,
    val sourceId: String,
    val nameArabic: String,
    val nameEnglish: String? = null,
    val photoUrl: String? = null,
    val country: String? = null,
    val biography: String? = null,
    val featured: Boolean = false,
    val metadata: Map<String, String> = emptyMap(),
)

data class Mushaf(
    val id: String,
    val sourceId: String,
    val reciterId: String,
    val name: String,
    val riwaya: String,
    val description: String? = null,
    val source: String? = null,
    val quality: String? = null,
    val format: String? = null,
    val totalSurahs: Int = 114,
    val availableSurahs: Set<Int> = emptySet(),
    val artworkUrl: String? = null,
)

data class SurahAudio(
    val id: String,
    val sourceId: String,
    val mushafId: String,
    val surahNumber: Int,
    val surahNameArabic: String,
    val surahNameEnglish: String? = null,
    val audioUrl: String,
    val backupUrls: List<String> = emptyList(),
    val durationMs: Long? = null,
    val fileSizeBytes: Long? = null,
    val format: String? = null,
    val bitrateKbps: Int? = null,
    val quality: String? = null,
    val checksum: String? = null,
    val downloadable: Boolean = true,
    val assetRightsStatus: AssetRightsStatus = AssetRightsStatus.INHERIT_SOURCE,
    val reciterId: String? = null,
    val metadata: Map<String, String> = emptyMap(),
)

data class QuranLanguage(val id: String, val code: String, val name: String, val nativeName: String)
data class QuranSurah(val number: Int, val name: String, val startPage: Int?, val endPage: Int?, val isMakki: Boolean?)
data class Riwaya(val id: Int, val name: String)

enum class PlaybackItemType { RADIO_STREAM, SURAH_AUDIO, LOCAL_RECORDING, DOWNLOADED_SURAH }

data class PlaybackItem(
    val id: String,
    val type: PlaybackItemType,
    val title: String,
    val subtitle: String? = null,
    val artwork: String? = null,
    val sourceId: String,
    val streamUri: String? = null,
    val localUri: String? = null,
    val mimeType: String? = null,
    val isLive: Boolean = false,
    val durationMs: Long? = null,
    val capabilities: ContentCapabilities,
    val metadata: Map<String, String> = emptyMap(),
)


@JvmInline
value class CanonicalReciterId(val value: String)

data class SourceReciterMapping(
    val canonicalReciterId: CanonicalReciterId,
    val sourceId: String,
    val sourceReciterId: String,
)
