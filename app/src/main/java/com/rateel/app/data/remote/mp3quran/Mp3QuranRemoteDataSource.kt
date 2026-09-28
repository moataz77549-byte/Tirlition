package com.rateel.app.data.remote.mp3quran

import com.rateel.app.core.model.AppResult
import com.rateel.app.data.remote.*
import com.rateel.app.domain.model.*
import java.io.IOException
import java.net.SocketTimeoutException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.SerializationException
import retrofit2.HttpException

@Singleton
class Mp3QuranRemoteDataSource @Inject constructor(
    private val api: Mp3QuranApi,
) : CatalogRemoteDataSource, RadioRemoteDataSource, ReciterRemoteDataSource, QuranAudioRemoteDataSource {

    override suspend fun fetchCatalog(language: String): AppResult<RemoteCatalog> = safe {
        val languagesResponse = api.languages()
        val suwar = api.suwar(language)
        val riwayat = api.riwayat(language)
        val reciters = api.reciters(language)
        val mapped = Mp3QuranMapper.mapCatalog(reciters, suwar)
        RemoteCatalog(
            languages = languagesResponse.languages.map { QuranLanguage(it.id, it.locale ?: "", it.language, it.native) },
            surahs = suwar.suwar.map { QuranSurah(it.id, it.name.trim(), it.startPage, it.endPage, it.makkia?.let { value -> value == 1 }) },
            riwayat = riwayat.riwayat.map { Riwaya(it.id, it.name.trim()) },
            reciters = mapped.reciters,
            mushafs = mapped.mushafs,
            tracks = mapped.tracks,
        )
    }

    override suspend fun fetchRadios(): AppResult<List<RadioStation>> = safe {
        val radios = Mp3QuranMapper.mapRadios(api.radios("ar"))
        val liveTv = runCatching { Mp3QuranMapper.mapLiveTv(api.liveTv("ar")) }.getOrDefault(emptyList())
        radios + liveTv
    }

    override suspend fun fetchReciters(): AppResult<List<Reciter>> =
        when (val result = fetchCatalog("ar")) {
            is AppResult.Success -> AppResult.Success(result.data.reciters)
            AppResult.Empty -> AppResult.Empty
            is AppResult.Error -> result
        }

    override suspend fun fetchMushafs(reciterId: String): AppResult<List<Mushaf>> =
        when (val result = fetchCatalog("ar")) {
            is AppResult.Success -> AppResult.Success(result.data.mushafs.filter { it.reciterId == reciterId })
            AppResult.Empty -> AppResult.Empty
            is AppResult.Error -> result
        }

    override suspend fun fetchTracks(mushafId: String): AppResult<List<SurahAudio>> =
        when (val result = fetchCatalog("ar")) {
            is AppResult.Success -> AppResult.Success(result.data.tracks.filter { it.mushafId == mushafId })
            AppResult.Empty -> AppResult.Empty
            is AppResult.Error -> result
        }

    private suspend fun <T> safe(block: suspend () -> T): AppResult<T> =
        try {
            val value = block()
            if (value is Collection<*> && value.isEmpty()) AppResult.Empty else AppResult.Success(value)
        } catch (_: SocketTimeoutException) {
            AppResult.Error.Timeout
        } catch (_: SerializationException) {
            AppResult.Error.Parsing
        } catch (e: HttpException) {
            if (e.code() >= 500) AppResult.Error.Server else AppResult.Error.Network
        } catch (_: IOException) {
            AppResult.Error.Network
        } catch (t: Throwable) {
            AppResult.Error.Unknown(t)
        }
}
