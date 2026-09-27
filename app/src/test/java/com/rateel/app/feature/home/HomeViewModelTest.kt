package com.rateel.app.feature.home

import com.rateel.app.core.model.AppResult
import com.rateel.app.core.network.NetworkStatusProvider
import com.rateel.app.domain.model.RadioStation
import com.rateel.app.domain.model.Reciter
import com.rateel.app.domain.model.SourceIds
import com.rateel.app.domain.repository.RadioRepository
import com.rateel.app.domain.repository.ReciterRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun repository_data_reaches_ui_state() = runTest(dispatcher) {
        val radio = RadioStation(
            id = "r1",
            sourceId = SourceIds.MP3_QURAN_V3,
            canonicalKey = "source:r1",
            nameArabic = "إذاعة",
            streams = emptyList(),
            isFeatured = true,
        )
        val reciter = Reciter(
            id = "q1",
            sourceId = SourceIds.MP3_QURAN_V3,
            nameArabic = "قارئ",
            featured = true,
        )
        val viewModel = HomeViewModel(
            radios = FakeRadioRepository(listOf(radio)),
            reciters = FakeReciterRepository(listOf(reciter)),
            network = object : NetworkStatusProvider {
                override val isOnline: Flow<Boolean> = MutableStateFlow(true)
            },
        )

        val collection = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.loading)
        assertFalse(viewModel.uiState.value.offline)
        assertEquals("r1", viewModel.uiState.value.featuredRadios.single().id)
        assertEquals("q1", viewModel.uiState.value.featuredReciters.single().id)
        collection.cancel()
    }

    private class FakeRadioRepository(items: List<RadioStation>) : RadioRepository {
        private val flow = MutableStateFlow(items)
        override fun observeRadios(): Flow<List<RadioStation>> = flow
        override suspend fun refresh(): AppResult<Unit> = AppResult.Success(Unit)
    }

    private class FakeReciterRepository(items: List<Reciter>) : ReciterRepository {
        private val flow = MutableStateFlow(items)
        override fun observeReciters(): Flow<List<Reciter>> = flow
        override suspend fun refresh(): AppResult<Unit> = AppResult.Success(Unit)
    }
}
