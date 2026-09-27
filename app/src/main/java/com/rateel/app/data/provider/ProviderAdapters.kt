package com.rateel.app.data.provider

import com.rateel.app.core.model.AppResult
import com.rateel.app.domain.model.Mushaf
import com.rateel.app.domain.model.RadioStation
import com.rateel.app.domain.model.Reciter
import com.rateel.app.domain.model.SurahAudio

/**
 * Stage-2 boundary for MP3Quran API v3. Production responses must be tagged with
 * sourceId=mp3quran-v3 before entering repositories.
 */
interface Mp3QuranV3Adapter {
    suspend fun radios(language: String = "ar"): AppResult<List<RadioStation>>
    suspend fun reciters(language: String = "ar"): AppResult<List<Reciter>>
    suspend fun mushafs(reciterId: String): AppResult<List<Mushaf>>
    suspend fun tracks(mushafId: String): AppResult<List<SurahAudio>>
}

/**
 * Quran Foundation credentials requiring a client secret must stay on a Rateel backend.
 * Android talks only to a Rateel gateway; it never ships client_secret in the APK.
 */
interface QuranFoundationAdapter {
    suspend fun recitationCatalog(): AppResult<List<Reciter>>
}
