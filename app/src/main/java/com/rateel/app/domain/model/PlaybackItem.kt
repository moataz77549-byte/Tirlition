package com.rateel.app.domain.model

enum class PlaybackType {
    RADIO_STREAM, SURAH_AUDIO, LOCAL_RECORDING, DOWNLOADED_SURAH, LOCAL_AUDIO,
}

data class PlaybackItem(
    val id: String,
    val type: PlaybackType,
    val title: String,
    val subtitle: String?,
    val artwork: String?,
    val sourceId: String,
    val remoteUri: String?,
    val localUri: String?,
    val mimeType: String?,
    val isLive: Boolean,
    val durationMs: Long?,
    val reciterId: String? = null,
    val mushafId: String? = null,
    val surahNumber: Int? = null,
    val stationId: String? = null,
    val capabilities: ContentCapabilities,
    val metadata: Map<String, String> = emptyMap(),
)

fun RadioStation.toPlaybackItem(endpoint: StreamEndpoint, capabilities: ContentCapabilities): PlaybackItem =
    PlaybackItem(
        id = id, type = PlaybackType.RADIO_STREAM, title = nameArabic,
        subtitle = nameEnglish, artwork = logoUrl, sourceId = endpoint.sourceId,
        remoteUri = endpoint.url, localUri = null,
        mimeType = when (endpoint.format?.lowercase()) {
            "hls", "m3u8" -> "application/x-mpegURL"
            "mp3" -> "audio/mpeg"
            "aac", "aac+" -> "audio/aac"
            else -> null
        },
        isLive = true, durationMs = null, stationId = id, capabilities = capabilities,
        metadata = mapOf("catalogSourceId" to sourceId),
    )

fun SurahAudio.toPlaybackItem(
    reciterId: String?,
    subtitle: String?,
    capabilities: ContentCapabilities,
): PlaybackItem = PlaybackItem(
    id = id, type = PlaybackType.SURAH_AUDIO, title = surahNameArabic,
    subtitle = subtitle, artwork = null, sourceId = sourceId, remoteUri = audioUrl,
    localUri = null, mimeType = if (format == "mp3") "audio/mpeg" else null,
    isLive = false, durationMs = durationMs, reciterId = reciterId,
    mushafId = mushafId, surahNumber = surahNumber, capabilities = capabilities,
)
