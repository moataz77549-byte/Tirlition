package com.rateel.app.debug

import com.rateel.app.core.model.AppResult
import com.rateel.app.domain.model.Mushaf
import com.rateel.app.domain.model.RadioStation
import com.rateel.app.domain.model.Reciter
import com.rateel.app.domain.model.SourceIds
import com.rateel.app.domain.model.StreamEndpoint
import com.rateel.app.domain.model.SurahAudio
import com.rateel.app.domain.repository.AudioRepository
import com.rateel.app.domain.repository.MushafRepository
import com.rateel.app.domain.repository.RadioRepository
import com.rateel.app.domain.repository.ReciterRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeRadioRepository(
    items: List<RadioStation> = listOf(
        RadioStation(
            id = "preview-radio",
            sourceId = SourceIds.MP3_QURAN_V3,
            canonicalKey = "preview:radio",
            nameArabic = "إذاعة تجريبية",
            streams = listOf(
                StreamEndpoint(
                    sourceId = SourceIds.QURANGO_STREAMS,
                    url = "https://example.invalid/radio",
                    format = "mp3",
                    primary = true,
                ),
            ),
            isFeatured = true,
        ),
    ),
) : RadioRepository {
    private val state = MutableStateFlow(items)
    override fun observeRadios(): Flow<List<RadioStation>> = state
    override suspend fun refresh(): AppResult<Unit> = AppResult.Success(Unit)
}

class FakeReciterRepository(
    items: List<Reciter> = listOf(
        Reciter(
            id = "preview-reciter",
            sourceId = SourceIds.MP3_QURAN_V3,
            nameArabic = "قارئ تجريبي",
            featured = true,
        ),
    ),
) : ReciterRepository {
    private val state = MutableStateFlow(items)
    override fun observeReciters(): Flow<List<Reciter>> = state
    override suspend fun refresh(): AppResult<Unit> = AppResult.Success(Unit)
}

class FakeMushafRepository(
    items: List<Mushaf> = emptyList(),
) : MushafRepository {
    private val state = MutableStateFlow(items)
    override fun observeMushafs(reciterId: String): Flow<List<Mushaf>> = state
}

class FakeAudioRepository(
    items: List<SurahAudio> = emptyList(),
) : AudioRepository {
    private val state = MutableStateFlow(items)
    override fun observeTracks(mushafId: String): Flow<List<SurahAudio>> = state
}
