package com.rateel.app.data.remote.mp3quran

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Query

@Serializable data class Mp3QuranLanguagesResponse(@SerialName("language") val languages: List<Mp3QuranLanguageDto> = emptyList())
@Serializable data class Mp3QuranLanguageDto(val id: String, val language: String, val native: String, val locale: String? = null, val surah: String? = null, val rewayah: String? = null, val reciters: String? = null, val radios: String? = null)
@Serializable data class Mp3QuranSuwarResponse(val suwar: List<Mp3QuranSurahDto> = emptyList())
@Serializable data class Mp3QuranSurahDto(val id: Int, val name: String, @SerialName("start_page") val startPage: Int? = null, @SerialName("end_page") val endPage: Int? = null, val makkia: Int? = null, val type: Int? = null)
@Serializable data class Mp3QuranRiwayatResponse(val riwayat: List<Mp3QuranRiwayaDto> = emptyList())
@Serializable data class Mp3QuranRiwayaDto(val id: Int, val name: String)
@Serializable data class Mp3QuranRecitersResponse(val reciters: List<Mp3QuranReciterDto> = emptyList())
@Serializable data class Mp3QuranReciterDto(val id: Int, val name: String, val letter: String? = null, val date: String? = null, val moshaf: List<Mp3QuranMushafDto> = emptyList())
@Serializable data class Mp3QuranMushafDto(val id: Int, val name: String, val server: String, @SerialName("surah_total") val surahTotal: Int = 0, @SerialName("moshaf_type") val moshafType: Int? = null, @SerialName("surah_list") val surahList: String = "")
@Serializable data class Mp3QuranRadiosResponse(val radios: List<Mp3QuranRadioDto> = emptyList())
@Serializable data class Mp3QuranRadioDto(val id: Int, val name: String, val url: String)
@Serializable data class Mp3QuranLiveTvResponse(@SerialName("livetv") val liveTv: List<Mp3QuranLiveTvDto> = emptyList())
@Serializable data class Mp3QuranLiveTvDto(val id: Int, val name: String, val url: String)
@Serializable data class Mp3QuranRecentReadsResponse(val reads: List<Mp3QuranRecentReadDto> = emptyList())
@Serializable data class Mp3QuranRecentReadDto(val id: Int, val name: String, val letter: String? = null, @SerialName("recent_date") val recentDate: String? = null, val moshaf: List<Mp3QuranMushafDto> = emptyList())

interface Mp3QuranApi {
    @GET("languages") suspend fun languages(): Mp3QuranLanguagesResponse
    @GET("suwar") suspend fun suwar(@Query("language") language: String = "ar"): Mp3QuranSuwarResponse
    @GET("riwayat") suspend fun riwayat(@Query("language") language: String = "ar"): Mp3QuranRiwayatResponse
    @GET("reciters") suspend fun reciters(@Query("language") language: String = "ar", @Query("reciter") reciter: Int? = null, @Query("rewaya") riwaya: Int? = null, @Query("sura") surah: Int? = null, @Query("last_updated_date") lastUpdatedDate: String? = null): Mp3QuranRecitersResponse
    @GET("recent_reads") suspend fun recentReads(@Query("language") language: String = "ar"): Mp3QuranRecentReadsResponse
    @GET("radios") suspend fun radios(@Query("language") language: String = "ar", @Query("last_updated_date") lastUpdatedDate: String? = null): Mp3QuranRadiosResponse
    @GET("live-tv") suspend fun liveTv(@Query("language") language: String = "ar"): Mp3QuranLiveTvResponse
}
