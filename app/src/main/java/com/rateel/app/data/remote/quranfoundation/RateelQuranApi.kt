package com.rateel.app.data.remote.quranfoundation

import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Path

@JvmInline value class ChapterReciterId(val value: Int)
@JvmInline value class AyahRecitationId(val value: Int)

@Serializable
data class RateelChapterAudioDto(
    val id: Int,
    val chapterId: Int,
    val fileSize: Long? = null,
    val format: String? = null,
    val audioUrl: String,
)

/**
 * Contract for a future Rateel backend. Android never receives Quran Foundation client_secret.
 */
interface RateelQuranApi {
    @GET("quran/chapter-recitations/{reciterId}/{chapterId}")
    suspend fun chapterAudio(
        @Path("reciterId") reciterId: Int,
        @Path("chapterId") chapterId: Int,
    ): RateelChapterAudioDto
}
