package com.rateel.app.data.repository

import com.rateel.app.core.model.AppResult
import com.rateel.app.data.local.RadioDao
import com.rateel.app.data.local.CacheMetadataDao
import com.rateel.app.data.local.CacheMetadataEntity
import com.rateel.app.data.local.RadioEntity
import com.rateel.app.data.local.RadioStreamEntity
import com.rateel.app.data.local.RadioWithStreams
import com.rateel.app.data.remote.RadioRemoteDataSource
import com.rateel.app.domain.model.RadioStation
import com.rateel.app.domain.model.SourceIds
import com.rateel.app.domain.model.StreamEndpoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OfflineRadioRepositoryTest {
    @Test
    fun refresh_persists_and_maps_source_metadata() = runTest {
        val remoteItem = RadioStation(
            id = "station-1",
            sourceId = SourceIds.MP3_QURAN_V3,
            canonicalKey = "mp3quran-v3:station-1",
            nameArabic = "إذاعة",
            streams = listOf(
                StreamEndpoint(
                    sourceId = SourceIds.QURANGO_STREAMS,
                    url = "https://qurango.net/radio/example",
                    format = "mp3",
                    primary = true,
                    providerEndpointId = "q1",
                ),
            ),
        )
        val dao = FakeRadioDao()
        val repository = OfflineRadioRepository(
            dao = dao,
            remote = object : RadioRemoteDataSource {
                override suspend fun fetchRadios(): AppResult<List<RadioStation>> =
                    AppResult.Success(listOf(remoteItem))
            },
            cache = object : CacheMetadataDao {
                override suspend fun get(key: String): CacheMetadataEntity? = null
                override suspend fun upsert(item: CacheMetadataEntity) = Unit
            },
        )

        assertTrue(repository.refresh() is AppResult.Success)
        val mapped = repository.observeRadios().first().single()

        assertEquals(SourceIds.MP3_QURAN_V3, mapped.sourceId)
        assertEquals("mp3quran-v3:station-1", mapped.canonicalKey)
        assertEquals(SourceIds.QURANGO_STREAMS, mapped.streams.single().sourceId)
        assertEquals("q1", mapped.streams.single().providerEndpointId)
    }

    private class FakeRadioDao : RadioDao() {
        private val rows = MutableStateFlow<List<RadioWithStreams>>(emptyList())

        override fun observeAll(): Flow<List<RadioWithStreams>> = rows

        protected override suspend fun insertStations(items: List<RadioEntity>) = Unit
        protected override suspend fun insertStreams(items: List<RadioStreamEntity>) = Unit

        override suspend fun replaceAll(
            stations: List<RadioEntity>,
            streams: List<RadioStreamEntity>,
        ) {
            rows.value = stations.map { station ->
                RadioWithStreams(
                    station = station,
                    streams = streams.filter { it.radioId == station.id },
                )
            }
        }
    }
}
