package com.rateel.app.data.remote

import com.rateel.app.core.model.AppResult
import com.rateel.app.domain.model.*
import java.io.IOException
import java.net.SocketTimeoutException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request

@Serializable data class Mp3QuranLanguagesResponse(val language: List<Mp3QuranLanguageDto> = emptyList())
@Serializable data class Mp3QuranLanguageDto(val locale: String = "", val native: String = "")
@Serializable data class Mp3QuranSuwarResponse(val suwar: List<Mp3QuranSurahDto> = emptyList())
@Serializable data class Mp3QuranSurahDto(
    val id: Int = 0, val name: String = "",
    @kotlinx.serialization.SerialName("start_page") val startPage: Int? = null,
    @kotlinx.serialization.SerialName("end_page") val endPage: Int? = null,
    val makkia: Int? = null,
)
@Serializable data class Mp3QuranRiwayatResponse(val riwayat: List<Mp3QuranRiwayaDto> = emptyList())
@Serializable data class Mp3QuranRiwayaDto(val id: Int = 0, val name: String = "")
@Serializable data class Mp3QuranRecitersResponse(val reciters: List<Mp3QuranReciterDto> = emptyList())
@Serializable data class Mp3QuranReciterDto(
    val id: Int = 0, val name: String = "",
    val moshaf: List<Mp3QuranMushafDto> = emptyList(),
)
@Serializable data class Mp3QuranMushafDto(
    val id: Int = 0, val name: String = "", val server: String = "",
    @kotlinx.serialization.SerialName("rewaya_id") val riwayaId: Int? = null,
    @kotlinx.serialization.SerialName("surah_total") val surahTotal: Int = 0,
    @kotlinx.serialization.SerialName("surah_list") val surahList: String = "",
    @kotlinx.serialization.SerialName("moshaf_type") val moshafType: Int? = null,
)
@Serializable data class Mp3QuranRadiosResponse(val radios: List<Mp3QuranRadioDto> = emptyList())
@Serializable data class Mp3QuranRadioDto(val id: Int = 0, val name: String = "", val url: String = "")
@Serializable data class Mp3QuranLiveTvResponse(val livetv: List<Mp3QuranLiveTvDto> = emptyList())
@Serializable data class Mp3QuranLiveTvDto(val id: Int = 0, val name: String = "", val url: String = "")

object Mp3QuranAudioUrlResolver {
    fun resolve(server: String, surahNumber: Int): String? {
        if (surahNumber !in 1..114) return null
        val url = server.trim()
        val normalizedUrl = if (url.startsWith("http://", ignoreCase = true)) {
            "https://" + url.substring(7)
        } else url
        if (!normalizedUrl.startsWith("https://", ignoreCase = true)) return null
        val host = runCatching { java.net.URI(normalizedUrl).host?.lowercase() }.getOrNull() ?: return null
        if (host != "mp3quran.net" && !host.endsWith(".mp3quran.net")) return null
        return normalizedUrl.trimEnd('/') + "/" + surahNumber.toString().padStart(3, '0') + ".mp3"
    }
}

internal fun availableSurahs(list: String): Set<Int> =
    list.split(',').mapNotNull { it.trim().toIntOrNull()?.takeIf { number -> number in 1..114 } }.toSet()

private fun safeAudioUrl(url: String): Boolean =
    runCatching {
        val scheme = java.net.URI(url).scheme?.lowercase()
        (scheme == "https" || scheme == "http") && !java.net.URI(url).host.isNullOrBlank()
    }.getOrDefault(false)

@Singleton
class Mp3QuranV3DataSource @Inject constructor(
    private val client: OkHttpClient,
) : RadioRemoteDataSource, ReciterRemoteDataSource, QuranAudioRemoteDataSource {
    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }
    // Both origins appear in the provider's v3 documentation. The canonical host is
    // tried first; the documented www host is a bounded fallback for connection errors.
    private val origins = listOf("https://mp3quran.net/api/v3/", "https://www.mp3quran.net/api/v3/")
    private var cachedReciters: Mp3QuranRecitersResponse? = null
    private var cachedSuwar: Mp3QuranSuwarResponse? = null
    private var cachedRiwayat: Mp3QuranRiwayatResponse? = null

    private suspend inline fun <reified T> load(path: String): AppResult<T> = withContext(Dispatchers.IO) {
        var lastError: AppResult.Error = AppResult.Error.Network
        for (origin in origins) {
            try {
                client.newCall(Request.Builder().url(origin + path).build()).execute().use { response ->
                    if (!response.isSuccessful) {
                        lastError = AppResult.Error.Server
                    } else {
                        val body = response.body?.string() ?: return@withContext AppResult.Error.Parsing
                        return@withContext AppResult.Success(json.decodeFromString<T>(body))
                    }
                }
            } catch (_: SocketTimeoutException) {
                lastError = AppResult.Error.Timeout
            } catch (_: IOException) {
                lastError = AppResult.Error.Network
            } catch (_: kotlinx.serialization.SerializationException) {
                return@withContext AppResult.Error.Parsing
            }
        }
        lastError
    }

    suspend fun languages(): AppResult<List<Mp3QuranLanguageDto>> =
        when (val result = load<Mp3QuranLanguagesResponse>("languages")) {
            is AppResult.Success -> AppResult.Success(result.data.language)
            AppResult.Empty -> AppResult.Empty
            is AppResult.Error -> result
        }

    private suspend fun suwar(): AppResult<Mp3QuranSuwarResponse> {
        cachedSuwar?.let { return AppResult.Success(it) }
        val result = load<Mp3QuranSuwarResponse>("suwar?language=ar")
        if (result is AppResult.Success) cachedSuwar = result.data
        return result
    }

    override suspend fun fetchSurahMetadata(): AppResult<List<SurahMetadata>> = when (val result = suwar()) {
        is AppResult.Success -> AppResult.Success(result.data.suwar.mapNotNull {
            if (it.id !in 1..114 || it.name.isBlank()) null
            else SurahMetadata(it.id, it.name, it.startPage, it.endPage, it.makkia?.let { value -> value == 1 })
        })
        AppResult.Empty -> AppResult.Empty
        is AppResult.Error -> result
    }

    private suspend fun riwayat(): AppResult<Mp3QuranRiwayatResponse> {
        cachedRiwayat?.let { return AppResult.Success(it) }
        val result = load<Mp3QuranRiwayatResponse>("riwayat?language=ar")
        if (result is AppResult.Success) cachedRiwayat = result.data
        return result
    }

    private suspend fun reciterCatalog(): AppResult<Mp3QuranRecitersResponse> {
        cachedReciters?.let { return AppResult.Success(it) }
        val result = load<Mp3QuranRecitersResponse>("reciters?language=ar")
        if (result is AppResult.Success) cachedReciters = result.data
        return result
    }

    override suspend fun fetchRadios(): AppResult<List<RadioStation>> {
        val result = load<Mp3QuranRadiosResponse>("radios?language=ar")
        if (result !is AppResult.Success) return result as AppResult<List<RadioStation>>
        val radios = result.data.radios.mapNotNull { dto ->
            val host = runCatching { java.net.URI(dto.url).host?.lowercase() }.getOrNull()
            if (dto.id <= 0 || dto.name.isBlank() || !safeAudioUrl(dto.url)) return@mapNotNull null
            val endpointSource = if (host == "qurango.net" || host?.endsWith(".qurango.net") == true) {
                SourceIds.QURANGO_STREAMS
            } else SourceIds.MP3_QURAN_V3
            RadioStation(
                id = "mp3quran:radio:${dto.id}", sourceId = SourceIds.MP3_QURAN_V3,
                canonicalKey = "mp3quran:${dto.id}", nameArabic = dto.name,
                streams = listOf(StreamEndpoint(endpointSource, dto.url, primary = true)),
                isVerified = true,
            )
        }
        val live = load<Mp3QuranLiveTvResponse>("live-tv")
        val channels = if (live is AppResult.Success) live.data.livetv.mapNotNull { dto ->
            if (dto.id <= 0 || dto.name.isBlank() || !safeAudioUrl(dto.url)) null
            else RadioStation(
                id = "mp3quran:live:${dto.id}", sourceId = SourceIds.MP3_QURAN_LIVE_TV,
                canonicalKey = "mp3quran:live:${dto.id}", nameArabic = dto.name,
                category = "AUDIO_FROM_LIVE_CHANNEL",
                streams = listOf(StreamEndpoint(SourceIds.MP3_QURAN_LIVE_TV, dto.url, format = "hls", primary = true)),
                isVerified = true,
            )
        } else emptyList()
        return if (radios.isEmpty() && channels.isEmpty()) AppResult.Empty else AppResult.Success(radios + channels)
    }

    override suspend fun fetchReciters(): AppResult<List<Reciter>> {
        val result = reciterCatalog()
        if (result !is AppResult.Success) return result as AppResult<List<Reciter>>
        val list = result.data.reciters.filter { it.id > 0 && it.name.isNotBlank() }.map {
            Reciter(id = "mp3quran:reciter:${it.id}", sourceId = SourceIds.MP3_QURAN_V3, nameArabic = it.name)
        }
        return if (list.isEmpty()) AppResult.Empty else AppResult.Success(list)
    }

    override suspend fun fetchMushafs(reciterId: String): AppResult<List<Mushaf>> {
        val result = reciterCatalog()
        if (result !is AppResult.Success) return result as AppResult<List<Mushaf>>
        val id = reciterId.substringAfterLast(':').toIntOrNull() ?: return AppResult.Empty
        val reciter = result.data.reciters.firstOrNull { it.id == id } ?: return AppResult.Empty
        val names = (riwayat() as? AppResult.Success)?.data?.riwayat?.associate { it.id to it.name }.orEmpty()
        val list = reciter.moshaf.mapNotNull { dto ->
            val available = availableSurahs(dto.surahList)
            if (dto.id <= 0 || available.isEmpty() || Mp3QuranAudioUrlResolver.resolve(dto.server, available.first()) == null) null
            else Mushaf(
                id = "mp3quran:mushaf:${dto.id}", sourceId = SourceIds.MP3_QURAN_V3,
                reciterId = reciterId, name = dto.name,
                riwaya = names[dto.riwayaId] ?: dto.name,
                totalSurahs = available.size, availableSurahs = available,
                source = dto.server,
            )
        }
        return if (list.isEmpty()) AppResult.Empty else AppResult.Success(list)
    }

    override suspend fun fetchTracks(mushafId: String): AppResult<List<SurahAudio>> {
        val result = reciterCatalog()
        if (result !is AppResult.Success) return result as AppResult<List<SurahAudio>>
        val id = mushafId.substringAfterLast(':').toIntOrNull() ?: return AppResult.Empty
        val dto = result.data.reciters.flatMap { it.moshaf }.firstOrNull { it.id == id } ?: return AppResult.Empty
        val names = (suwar() as? AppResult.Success)?.data?.suwar?.associate { it.id to it.name }.orEmpty()
        if (names.isEmpty()) return AppResult.Error.Parsing
        val tracks = availableSurahs(dto.surahList).sorted().mapNotNull { number ->
            val url = Mp3QuranAudioUrlResolver.resolve(dto.server, number) ?: return@mapNotNull null
            val name = names[number] ?: return@mapNotNull null
            SurahAudio(
                id = "$mushafId:$number", sourceId = SourceIds.MP3_QURAN_V3,
                mushafId = mushafId, surahNumber = number, surahNameArabic = name,
                audioUrl = url, format = "mp3",
            )
        }
        return if (tracks.isEmpty()) AppResult.Empty else AppResult.Success(tracks)
    }
}
