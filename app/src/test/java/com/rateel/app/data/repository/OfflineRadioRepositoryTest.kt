package com.rateel.app.data.repository

import com.rateel.app.core.model.AppResult
import com.rateel.app.data.local.*
import com.rateel.app.data.remote.RadioRemoteDataSource
import com.rateel.app.domain.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
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
                    returnedBySourceId = SourceIds.MP3_QURAN_V3,
                    url = "https://qurango.net/radio/example",
                    assetHost = "qurango.net",
                    format = "mp3",
                    primary = true,
                    providerEndpointId = "q1",
                    assetRightsStatus = AssetRightsStatus.STREAM_ONLY,
                ),
            ),
        )
        val dao = FakeRadioDao()
        val repository = OfflineRadioRepository(
            dao,
            FakeSyncDao(),
            object : RadioRemoteDataSource {
                override suspend fun fetchRadios(): AppResult<List<RadioStation>> = AppResult.Success(listOf(remoteItem))
            },
        )
        assertTrue(repository.refresh() is AppResult.Success)
        val mapped = repository.observeRadios().first().single()
        assertEquals(SourceIds.MP3_QURAN_V3, mapped.sourceId)
        assertEquals(SourceIds.QURANGO_STREAMS, mapped.streams.single().sourceId)
        assertEquals(SourceIds.MP3_QURAN_V3, mapped.streams.single().returnedBySourceId)
        assertEquals(AssetRightsStatus.STREAM_ONLY, mapped.streams.single().assetRightsStatus)
    }

    private class FakeSyncDao : SourceSyncDao {
        private val values = mutableMapOf<String, SourceSyncEntity>()
        override suspend fun get(syncKey: String): SourceSyncEntity? = values[syncKey]
        override suspend fun upsert(item: SourceSyncEntity) { values[item.syncKey] = item }
    }

    private class FakeRadioDao : RadioDao() {
        private val rows = MutableStateFlow<List<RadioWithStreams>>(emptyList())
        override fun observeAll(): Flow<List<RadioWithStreams>> = rows
        override suspend fun insertStations(items: List<RadioEntity>) = Unit
        override suspend fun insertStreams(items: List<RadioStreamEntity>) = Unit
        override suspend fun deleteBySource(sourceId: String) = Unit

        override suspend fun replaceBySource(sourceId: String, stations: List<RadioEntity>, streams: List<RadioStreamEntity>) {
            rows.value = stations.map { station ->
                RadioWithStreams(station, streams.filter { it.radioId == station.id })
            }
        }
    }
}
