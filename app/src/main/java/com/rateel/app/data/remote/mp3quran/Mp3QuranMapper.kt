package com.rateel.app.data.remote.mp3quran

import com.rateel.app.domain.model.*
import com.rateel.app.domain.source.StreamSourceResolver
import java.net.URI

data class Mp3QuranCatalog(
    val reciters: List<Reciter>,
    val mushafs: List<Mushaf>,
    val tracks: List<SurahAudio>,
)

object Mp3QuranMapper {
    fun parseSurahList(value: String): List<Int> =
        value.split(',').asSequence().map(String::trim).filter(String::isNotEmpty)
            .mapNotNull(String::toIntOrNull).filter { it in 1..114 }.distinct().sorted().toList()

    fun mapCatalog(response: Mp3QuranRecitersResponse, suwar: Mp3QuranSuwarResponse): Mp3QuranCatalog {
        val names = suwar.suwar.associate { it.id to it.name.trim() }
        val reciters = response.reciters.map { dto ->
            Reciter(
                id = "mp3quran:reciter:${dto.id}",
                sourceId = SourceIds.MP3_QURAN_V3,
                nameArabic = dto.name.trim(),
                metadata = buildMap {
                    dto.letter?.let { put("letter", it) }
                    dto.date?.let { put("sourceDate", it) }
                    put("remoteId", dto.id.toString())
                },
            )
        }
        val mushafs = response.reciters.flatMap { reciter ->
            reciter.moshaf.map { dto ->
                Mushaf(
                    id = "mp3quran:mushaf:${dto.id}",
                    sourceId = SourceIds.MP3_QURAN_V3,
                    reciterId = "mp3quran:reciter:${reciter.id}",
                    name = dto.name.trim(),
                    riwaya = dto.name.trim(),
                    source = dto.server,
                    format = "mp3",
                    totalSurahs = dto.surahTotal,
                    availableSurahs = parseSurahList(dto.surahList).toSet(),
                )
            }
        }
        val tracks = response.reciters.flatMap { reciter ->
            reciter.moshaf.flatMap { mushaf ->
                parseSurahList(mushaf.surahList).map { number ->
                    SurahAudio(
                        id = "mp3quran:track:${mushaf.id}:$number",
                        sourceId = SourceIds.MP3_QURAN_V3,
                        mushafId = "mp3quran:mushaf:${mushaf.id}",
                        surahNumber = number,
                        surahNameArabic = names[number] ?: number.toString(),
                        audioUrl = Mp3QuranAudioUrlResolver.resolve(mushaf.server, number),
                        format = "mp3",
                        downloadable = true,
                        assetRightsStatus = AssetRightsStatus.VERIFIED_ALLOWED,
                        metadata = mapOf("remoteMushafId" to mushaf.id.toString(), "remoteReciterId" to reciter.id.toString()),
                    )
                }
            }
        }
        return Mp3QuranCatalog(reciters, mushafs, tracks)
    }

    fun mapRadios(response: Mp3QuranRadiosResponse): List<RadioStation> =
        response.radios.map { dto ->
            val endpointSource = StreamSourceResolver.sourceIdFor(dto.url, SourceIds.MP3_QURAN_V3)
            RadioStation(
                id = "mp3quran:radio:${dto.id}",
                sourceId = SourceIds.MP3_QURAN_V3,
                canonicalKey = "mp3quran:radio:${dto.id}",
                nameArabic = dto.name.trim(),
                streams = listOf(
                    StreamEndpoint(
                        sourceId = endpointSource,
                        returnedBySourceId = SourceIds.MP3_QURAN_V3,
                        url = dto.url,
                        originalUrl = dto.url,
                        assetHost = hostOf(dto.url),
                        format = inferFormat(dto.url),
                        primary = true,
                        providerEndpointId = dto.id.toString(),
                        assetRightsStatus = if (endpointSource == SourceIds.QURANGO_STREAMS) AssetRightsStatus.STREAM_ONLY else AssetRightsStatus.PENDING_VERIFICATION,
                    ),
                ),
                category = "إذاعة قرآنية",
                categoryOrigin = CategoryOrigin.DERIVED,
            )
        }

    fun mapLiveTv(response: Mp3QuranLiveTvResponse): List<RadioStation> =
        response.liveTv.map { dto ->
            RadioStation(
                id = "mp3quran:live-tv:${dto.id}",
                sourceId = SourceIds.MP3_QURAN_LIVE_TV,
                canonicalKey = "mp3quran:live-tv:${dto.id}",
                nameArabic = dto.name.trim(),
                streams = listOf(
                    StreamEndpoint(
                        sourceId = SourceIds.MP3_QURAN_LIVE_TV,
                        returnedBySourceId = SourceIds.MP3_QURAN_V3,
                        url = dto.url,
                        originalUrl = dto.url,
                        assetHost = hostOf(dto.url),
                        format = inferFormat(dto.url),
                        primary = true,
                        providerEndpointId = dto.id.toString(),
                        assetRightsStatus = AssetRightsStatus.STREAM_ONLY,
                    ),
                ),
                category = "قناة مباشرة",
                categoryOrigin = CategoryOrigin.DERIVED,
                tags = listOf("live-tv", "audio-from-live-channel"),
            )
        }

    private fun inferFormat(url: String): String? = when {
        url.substringBefore('?').endsWith(".m3u8", ignoreCase = true) -> "hls"
        url.substringBefore('?').endsWith(".aac", ignoreCase = true) -> "aac"
        else -> "mp3"
    }

    private fun hostOf(url: String): String? = runCatching { URI(url).host?.lowercase() }.getOrNull()
}
