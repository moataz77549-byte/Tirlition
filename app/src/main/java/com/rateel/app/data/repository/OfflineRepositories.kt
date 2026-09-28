package com.rateel.app.data.repository

import com.rateel.app.core.model.AppResult
import com.rateel.app.data.local.*
import com.rateel.app.data.remote.CatalogRemoteDataSource
import com.rateel.app.data.remote.RadioRemoteDataSource
import com.rateel.app.domain.model.*
import com.rateel.app.domain.repository.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val METADATA_TTL_MS = 6 * 60 * 60 * 1000L

private fun RadioWithStreams.toDomain(): RadioStation =
    RadioStation(
        id = station.id, sourceId = station.sourceId, canonicalKey = station.canonicalKey,
        nameArabic = station.nameArabic, nameEnglish = station.nameEnglish, description = station.description,
        streams = streams.sortedByDescending { it.isPrimary }.map {
            StreamEndpoint(
                sourceId = it.sourceId, url = it.url, format = it.format, bitrateKbps = it.bitrateKbps,
                primary = it.isPrimary, providerEndpointId = it.providerEndpointId,
                returnedBySourceId = it.returnedBySourceId, originalUrl = it.originalUrl.ifBlank { it.url },
                resolvedUrl = it.resolvedUrl, assetHost = it.assetHost, resolvedHost = it.resolvedHost,
                lastResolvedAt = it.lastResolvedAt,
                health = runCatching { StreamHealth.valueOf(it.health) }.getOrDefault(StreamHealth.UNKNOWN),
                assetRightsStatus = runCatching { AssetRightsStatus.valueOf(it.assetRightsStatus) }.getOrDefault(AssetRightsStatus.INHERIT_SOURCE),
            )
        },
        logoUrl = station.logoUrl, country = station.country, language = station.language, category = station.category,
        categoryOrigin = runCatching { CategoryOrigin.valueOf(station.categoryOrigin) }.getOrDefault(CategoryOrigin.UNKNOWN),
        website = station.website, isActive = station.isActive, isFeatured = station.isFeatured, isVerified = station.isVerified,
        health = runCatching { StreamHealth.valueOf(station.health) }.getOrDefault(StreamHealth.UNKNOWN),
        sourceHealth = runCatching { SourceHealth.valueOf(station.sourceHealth) }.getOrDefault(SourceHealth.UNKNOWN),
        createdAt = station.createdAt, updatedAt = station.updatedAt,
    )

@Singleton
class OfflineRadioRepository @Inject constructor(
    private val dao: RadioDao,
    private val syncDao: SourceSyncDao,
    private val remote: RadioRemoteDataSource,
    private val sources: SourceRepository,
) : RadioRepository {
    override fun observeRadios(): Flow<List<RadioStation>> = dao.observeAll().map { rows -> rows.map { it.toDomain() } }

    override suspend fun refresh(): AppResult<Unit> {
        sources.ensureBuiltInCatalog()
        val now = System.currentTimeMillis()
        return when (val result = remote.fetchRadios()) {
            is AppResult.Success -> {
                result.data.groupBy { it.sourceId }.forEach { (sourceId, items) ->
                    val stations = items.map {
                        RadioEntity(
                            it.id, it.sourceId, it.canonicalKey, it.nameArabic, it.nameEnglish, it.description,
                            it.logoUrl, it.country, it.language, it.category, it.categoryOrigin.name, it.website,
                            it.isActive, it.isFeatured, it.isVerified, it.health.name, it.sourceHealth.name,
                            it.createdAt, it.updatedAt,
                        )
                    }
                    val streams = items.flatMap { station ->
                        station.streams.mapIndexed { index, stream ->
                            RadioStreamEntity(
                                station.id + ":" + (stream.providerEndpointId ?: index.toString()),
                                station.id, stream.sourceId, stream.providerEndpointId, stream.returnedBySourceId,
                                stream.url, stream.originalUrl, stream.resolvedUrl, stream.assetHost, stream.resolvedHost,
                                stream.lastResolvedAt, stream.format, stream.bitrateKbps, stream.primary,
                                stream.health.name, stream.assetRightsStatus.name,
                            )
                        }
                    }
                    dao.replaceBySource(sourceId, stations, streams)
                }
                syncDao.upsert(SourceSyncEntity("radios", SourceIds.MP3_QURAN_V3, now, now, now + METADATA_TTL_MS))
                AppResult.Success(Unit)
            }
            AppResult.Empty -> AppResult.Empty
            is AppResult.Error -> {
                val previous = syncDao.get("radios")
                syncDao.upsert(SourceSyncEntity("radios", SourceIds.MP3_QURAN_V3, now, previous?.lastSuccessfulSyncAt, previous?.expiresAt))
                result
            }
        }
    }

    override suspend fun refreshIfStale(): AppResult<Unit> {
        val sync = syncDao.get("radios")
        return if (sync?.expiresAt != null && sync.expiresAt > System.currentTimeMillis()) AppResult.Success(Unit) else refresh()
    }
}

@Singleton
class OfflineReciterRepository @Inject constructor(
    private val reciterDao: ReciterDao,
    private val catalogDao: CatalogDao,
    private val syncDao: SourceSyncDao,
    private val remote: CatalogRemoteDataSource,
    private val sources: SourceRepository,
) : ReciterRepository {
    override fun observeReciters(): Flow<List<Reciter>> =
        reciterDao.observeAll().map { rows ->
            rows.map { Reciter(it.id, it.sourceId, it.nameArabic, it.nameEnglish, it.photoUrl, it.country, it.biography, it.featured) }
        }

    override suspend fun refresh(): AppResult<Unit> {
        sources.ensureBuiltInCatalog()
        val now = System.currentTimeMillis()
        return when (val result = remote.fetchCatalog("ar")) {
            is AppResult.Success -> {
                val c = result.data
                catalogDao.replaceMp3QuranCatalog(
                    SourceIds.MP3_QURAN_V3,
                    c.reciters.map { ReciterEntity(it.id, it.sourceId, it.nameArabic, it.nameEnglish, it.photoUrl, it.country, it.biography, it.featured) },
                    c.mushafs.map { MushafEntity(it.id, it.sourceId, it.reciterId, it.name, it.riwaya, it.description, it.source, it.quality, it.format, it.totalSurahs, it.artworkUrl) },
                    c.tracks.map { AudioTrackEntity(it.id, it.sourceId, it.mushafId, it.surahNumber, it.surahNameArabic, it.surahNameEnglish, it.audioUrl, it.durationMs, it.fileSizeBytes, it.format, it.bitrateKbps, it.quality, it.checksum, it.downloadable, it.assetRightsStatus.name, it.reciterId) },
                    c.languages.map { QuranLanguageEntity(it.id, it.code, it.name, it.nativeName) },
                    c.surahs.map { QuranSurahEntity(it.number, it.name, it.startPage, it.endPage, it.isMakki) },
                    c.riwayat.map { RiwayaEntity(it.id, it.name) },
                )
                syncDao.upsert(SourceSyncEntity("catalog", SourceIds.MP3_QURAN_V3, now, now, now + METADATA_TTL_MS))
                AppResult.Success(Unit)
            }
            AppResult.Empty -> AppResult.Empty
            is AppResult.Error -> {
                val previous = syncDao.get("catalog")
                syncDao.upsert(SourceSyncEntity("catalog", SourceIds.MP3_QURAN_V3, now, previous?.lastSuccessfulSyncAt, previous?.expiresAt))
                result
            }
        }
    }

    override suspend fun refreshIfStale(): AppResult<Unit> {
        val sync = syncDao.get("catalog")
        return if (sync?.expiresAt != null && sync.expiresAt > System.currentTimeMillis()) AppResult.Success(Unit) else refresh()
    }
}

@Singleton
class LocalMushafRepository @Inject constructor(private val dao: MushafDao) : MushafRepository {
    override fun observeMushafs(reciterId: String): Flow<List<Mushaf>> =
        dao.observeByReciter(reciterId).map { rows ->
            rows.map { row ->
                val it = row.mushaf
                Mushaf(
                    id = it.id, sourceId = it.sourceId, reciterId = it.reciterId, name = it.name,
                    riwaya = it.riwaya, description = it.description, source = it.source,
                    quality = it.quality, format = it.format, totalSurahs = it.totalSurahs,
                    availableSurahs = row.tracks.map { track -> track.surahNumber }.toSet(),
                    artworkUrl = it.artworkUrl,
                )
            }
        }
}

@Singleton
class LocalAudioRepository @Inject constructor(private val dao: AudioTrackDao) : AudioRepository {
    override fun observeTracks(mushafId: String): Flow<List<SurahAudio>> =
        dao.observeByMushaf(mushafId).map { rows ->
            rows.map {
                SurahAudio(
                    id = it.id, sourceId = it.sourceId, mushafId = it.mushafId, surahNumber = it.surahNumber,
                    surahNameArabic = it.surahNameArabic, surahNameEnglish = it.surahNameEnglish, audioUrl = it.audioUrl,
                    durationMs = it.durationMs, fileSizeBytes = it.fileSizeBytes, format = it.format,
                    bitrateKbps = it.bitrateKbps, quality = it.quality, checksum = it.checksum,
                    downloadable = it.downloadable,
                    assetRightsStatus = runCatching { AssetRightsStatus.valueOf(it.assetRightsStatus) }.getOrDefault(AssetRightsStatus.INHERIT_SOURCE),
                    reciterId = it.reciterId,
                )
            }
        }
}
