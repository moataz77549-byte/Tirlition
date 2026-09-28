package com.rateel.app.feature.home

import com.rateel.app.core.model.AppResult
import com.rateel.app.core.network.NetworkStatusProvider
import com.rateel.app.domain.model.*
import com.rateel.app.domain.repository.RadioRepository
import com.rateel.app.domain.repository.ReciterRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    @Before fun setUp() { Dispatchers.setMain(dispatcher) }
    @After fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun repository_data_reaches_ui_state() = runTest(dispatcher) {
        val radio = RadioStation("r1", SourceIds.MP3_QURAN_V3, "source:r1", "إذاعة", streams = emptyList(), isFeatured = true)
        val reciter = Reciter("q1", SourceIds.MP3_QURAN_V3, "قارئ", featured = true)
        val viewModel = HomeViewModel(
            FakeRadioRepository(listOf(radio)),
            FakeReciterRepository(listOf(reciter)),
            object : NetworkStatusProvider { override val isOnline: Flow<Boolean> = MutableStateFlow(true) },
        )
        val collection = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
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
        override suspend fun refreshIfStale(): AppResult<Unit> = AppResult.Success(Unit)
    }
    private class FakeReciterRepository(items: List<Reciter>) : ReciterRepository {
        private val flow = MutableStateFlow(items)
        override fun observeReciters(): Flow<List<Reciter>> = flow
        override suspend fun refresh(): AppResult<Unit> = AppResult.Success(Unit)
        override suspend fun refreshIfStale(): AppResult<Unit> = AppResult.Success(Unit)
    }
}
