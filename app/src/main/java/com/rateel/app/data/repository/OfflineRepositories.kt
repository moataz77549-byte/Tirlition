package com.rateel.app.data.repository

import com.rateel.app.core.model.AppResult
import com.rateel.app.data.local.AudioTrackDao
import com.rateel.app.data.local.AudioTrackEntity
import com.rateel.app.data.local.CacheMetadataDao
import com.rateel.app.data.local.CacheMetadataEntity
import com.rateel.app.data.local.MushafDao
import com.rateel.app.data.local.MushafEntity
import com.rateel.app.data.local.RadioDao
import com.rateel.app.data.local.RadioEntity
import com.rateel.app.data.local.RadioStreamEntity
import com.rateel.app.data.local.ReciterDao
import com.rateel.app.data.local.ReciterEntity
import com.rateel.app.data.local.SurahMetadataDao
import com.rateel.app.data.local.SurahMetadataEntity
import com.rateel.app.data.remote.RadioRemoteDataSource
import com.rateel.app.data.remote.ReciterRemoteDataSource
import com.rateel.app.data.remote.QuranAudioRemoteDataSource
import com.rateel.app.domain.model.Mushaf
import com.rateel.app.domain.model.RadioStation
import com.rateel.app.domain.model.Reciter
import com.rateel.app.domain.model.StreamEndpoint
import com.rateel.app.domain.model.StreamHealth
import com.rateel.app.domain.model.SurahAudio
import com.rateel.app.domain.repository.AudioRepository
import com.rateel.app.domain.repository.MushafRepository
import com.rateel.app.domain.repository.RadioRepository
import com.rateel.app.domain.repository.ReciterRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val CATALOG_TTL_MS = 12L * 60 * 60 * 1000

private fun com.rateel.app.data.local.RadioWithStreams.toDomain(): RadioStation =
    RadioStation(
        id = station.id,
        sourceId = station.sourceId,
        canonicalKey = station.canonicalKey,
        nameArabic = station.nameArabic,
        nameEnglish = station.nameEnglish,
        description = station.description,
        streams = streams.sortedByDescending { it.isPrimary }.map {
            StreamEndpoint(
                sourceId = it.sourceId,
                url = it.url,
                format = it.format,
                bitrateKbps = it.bitrateKbps,
                primary = it.isPrimary,
                providerEndpointId = it.providerEndpointId,
            )
        },
        logoUrl = station.logoUrl,
        country = station.country,
        language = station.language,
        category = station.category,
        website = station.website,
        isActive = station.isActive,
        isFeatured = station.isFeatured,
        isVerified = station.isVerified,
        health = runCatching { StreamHealth.valueOf(station.health) }
            .getOrDefault(StreamHealth.UNKNOWN),
        createdAt = station.createdAt,
        updatedAt = station.updatedAt,
    )

@Singleton
class OfflineRadioRepository @Inject constructor(
    private val dao: RadioDao,
    private val remote: RadioRemoteDataSource,
    private val cache: CacheMetadataDao,
) : RadioRepository {
    override fun observeRadios(): Flow<List<RadioStation>> =
        dao.observeAll().map { items -> items.map { it.toDomain() } }

    override suspend fun refresh(): AppResult<Unit> =
        when (val result = remote.fetchRadios()) {
            is AppResult.Success -> {
                val stations = result.data.map {
                    RadioEntity(
                        id = it.id,
                        sourceId = it.sourceId,
                        canonicalKey = it.canonicalKey,
                        nameArabic = it.nameArabic,
                        nameEnglish = it.nameEnglish,
                        description = it.description,
                        logoUrl = it.logoUrl,
                        country = it.country,
                        language = it.language,
                        category = it.category,
                        website = it.website,
                        isActive = it.isActive,
                        isFeatured = it.isFeatured,
                        isVerified = it.isVerified,
                        health = it.health.name,
                        createdAt = it.createdAt,
                        updatedAt = it.updatedAt,
                    )
                }
                val streams = result.data.flatMap { station ->
                    station.streams.mapIndexed { index, stream ->
                        RadioStreamEntity(
                            id = station.id + ":" + (stream.providerEndpointId ?: index.toString()),
                            radioId = station.id,
                            sourceId = stream.sourceId,
                            providerEndpointId = stream.providerEndpointId,
                            url = stream.url,
                            format = stream.format,
                            bitrateKbps = stream.bitrateKbps,
                            isPrimary = stream.primary,
                        )
                    }
                }
                dao.replaceAll(stations, streams)
                cache.upsert(CacheMetadataEntity("mp3quran:radios", System.currentTimeMillis(),
                    System.currentTimeMillis() + CATALOG_TTL_MS))
                AppResult.Success(Unit)
            }
            AppResult.Empty -> AppResult.Empty
            is AppResult.Error -> result
        }

    override suspend fun refreshIfStale(): AppResult<Unit> {
        if ((cache.get("mp3quran:radios")?.expiresAt ?: 0) > System.currentTimeMillis()) {
            return AppResult.Success(Unit)
        }
        return refresh()
    }
}

@Singleton
class OfflineReciterRepository @Inject constructor(
    private val dao: ReciterDao,
    private val remote: ReciterRemoteDataSource,
    private val cache: CacheMetadataDao,
) : ReciterRepository {
    override fun observeReciters(): Flow<List<Reciter>> =
        dao.observeAll().map { items ->
            items.map {
                Reciter(
                    id = it.id,
                    sourceId = it.sourceId,
                    nameArabic = it.nameArabic,
                    nameEnglish = it.nameEnglish,
                    photoUrl = it.photoUrl,
                    country = it.country,
                    biography = it.biography,
                    featured = it.featured,
                )
            }
        }

    override suspend fun refresh(): AppResult<Unit> =
        when (val result = remote.fetchReciters()) {
            is AppResult.Success -> {
                dao.upsertAll(
                    result.data.map {
                        ReciterEntity(
                            id = it.id,
                            sourceId = it.sourceId,
                            nameArabic = it.nameArabic,
                            nameEnglish = it.nameEnglish,
                            photoUrl = it.photoUrl,
                            country = it.country,
                            biography = it.biography,
                            featured = it.featured,
                        )
                    },
                )
                cache.upsert(CacheMetadataEntity("mp3quran:reciters", System.currentTimeMillis(),
                    System.currentTimeMillis() + CATALOG_TTL_MS))
                AppResult.Success(Unit)
            }
            AppResult.Empty -> AppResult.Empty
            is AppResult.Error -> result
        }

    override suspend fun refreshIfStale(): AppResult<Unit> {
        if ((cache.get("mp3quran:reciters")?.expiresAt ?: 0) > System.currentTimeMillis()) {
            return AppResult.Success(Unit)
        }
        return refresh()
    }
}

@Singleton
class LocalMushafRepository @Inject constructor(
    private val dao: MushafDao,
    private val remote: QuranAudioRemoteDataSource,
) : MushafRepository {
    override fun observeMushafs(reciterId: String): Flow<List<Mushaf>> =
        dao.observeByReciter(reciterId).map { items ->
            items.map {
                Mushaf(
                    id = it.id,
                    sourceId = it.sourceId,
                    reciterId = it.reciterId,
                    name = it.name,
                    riwaya = it.riwaya,
                    description = it.description,
                    source = it.source,
                    quality = it.quality,
                    format = it.format,
                    totalSurahs = it.totalSurahs,
                    artworkUrl = it.artworkUrl,
                    availableSurahs = it.availableSurahs.split(',').mapNotNull { value -> value.toIntOrNull() }.toSet(),
                )
            }
        }

    override suspend fun refresh(reciterId: String): AppResult<Unit> =
        when (val result = remote.fetchMushafs(reciterId)) {
            is AppResult.Success -> {
                dao.upsertAll(result.data.map {
                    MushafEntity(
                        id = it.id, sourceId = it.sourceId, reciterId = it.reciterId,
                        name = it.name, riwaya = it.riwaya, description = it.description,
                        source = it.source, quality = it.quality, format = it.format,
                        totalSurahs = it.totalSurahs, artworkUrl = it.artworkUrl,
                        availableSurahs = it.availableSurahs.sorted().joinToString(","),
                    )
                })
                AppResult.Success(Unit)
            }
            AppResult.Empty -> AppResult.Empty
            is AppResult.Error -> result
        }
}

@Singleton
class LocalAudioRepository @Inject constructor(
    private val dao: AudioTrackDao,
    private val remote: QuranAudioRemoteDataSource,
    private val surahDao: SurahMetadataDao,
) : AudioRepository {
    override fun observeTracks(mushafId: String): Flow<List<SurahAudio>> =
        dao.observeByMushaf(mushafId).map { items ->
            items.map {
                SurahAudio(
                    id = it.id,
                    sourceId = it.sourceId,
                    mushafId = it.mushafId,
                    surahNumber = it.surahNumber,
                    surahNameArabic = it.surahNameArabic,
                    surahNameEnglish = it.surahNameEnglish,
                    audioUrl = it.audioUrl,
                    durationMs = it.durationMs,
                    fileSizeBytes = it.fileSizeBytes,
                    format = it.format,
                    bitrateKbps = it.bitrateKbps,
                    quality = it.quality,
                    checksum = it.checksum,
                    downloadable = it.downloadable,
                )
            }
        }

    override suspend fun refresh(mushafId: String): AppResult<Unit> =
        when (val result = remote.fetchTracks(mushafId)) {
            is AppResult.Success -> {
                dao.upsertAll(result.data.map {
                    AudioTrackEntity(
                        id = it.id, sourceId = it.sourceId, mushafId = it.mushafId,
                        surahNumber = it.surahNumber, surahNameArabic = it.surahNameArabic,
                        surahNameEnglish = it.surahNameEnglish, audioUrl = it.audioUrl,
                        durationMs = it.durationMs, fileSizeBytes = it.fileSizeBytes,
                        format = it.format, bitrateKbps = it.bitrateKbps, quality = it.quality,
                        checksum = it.checksum, downloadable = it.downloadable,
                    )
                })
                val metadata = remote.fetchSurahMetadata()
                if (metadata is AppResult.Success) {
                    surahDao.upsertAll(metadata.data.map {
                        SurahMetadataEntity(it.number, it.name, it.startPage, it.endPage, it.isMakki)
                    })
                }
                AppResult.Success(Unit)
            }
            AppResult.Empty -> AppResult.Empty
            is AppResult.Error -> result
        }
}
