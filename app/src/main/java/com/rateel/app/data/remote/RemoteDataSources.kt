package com.rateel.app.data.remote

import com.rateel.app.core.model.AppResult
import com.rateel.app.domain.model.*

data class RemoteCatalog(
    val languages: List<QuranLanguage>,
    val surahs: List<QuranSurah>,
    val riwayat: List<Riwaya>,
    val reciters: List<Reciter>,
    val mushafs: List<Mushaf>,
    val tracks: List<SurahAudio>,
)

interface CatalogRemoteDataSource { suspend fun fetchCatalog(language: String = "ar"): AppResult<RemoteCatalog> }
interface RadioRemoteDataSource { suspend fun fetchRadios(): AppResult<List<RadioStation>> }
interface ReciterRemoteDataSource { suspend fun fetchReciters(): AppResult<List<Reciter>> }
interface QuranAudioRemoteDataSource { suspend fun fetchMushafs(reciterId: String): AppResult<List<Mushaf>>; suspend fun fetchTracks(mushafId: String): AppResult<List<SurahAudio>> }
