package com.rateel.app.domain.model

enum class StreamHealth { ONLINE, OFFLINE, UNKNOWN, DEGRADED }

data class StreamEndpoint(
    val sourceId: String,
    val url: String,
    val format: String? = null,
    val bitrateKbps: Int? = null,
    val primary: Boolean = false,
    val providerEndpointId: String? = null,
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
    val website: String? = null,
    val isActive: Boolean = true,
    val isFeatured: Boolean = false,
    val isVerified: Boolean = false,
    val health: StreamHealth = StreamHealth.UNKNOWN,
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
    val metadata: Map<String, String> = emptyMap(),
)

data class LocalRecording(
    val id: String,
    val stationId: String,
    val sourceId: String,
    val stationName: String,
    val title: String,
    val filePath: String,
    val mimeType: String,
    val durationMs: Long?,
    val fileSizeBytes: Long?,
    val startedAt: Long,
    val finishedAt: Long?,
    val recordingMode: String,
    val requestedDurationMs: Long?,
    val artwork: String?,
    val sourceAttribution: String?,
    val createdAt: Long,
)
