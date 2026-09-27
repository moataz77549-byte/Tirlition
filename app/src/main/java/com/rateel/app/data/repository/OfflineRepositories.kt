package com.rateel.app.data.repository

import com.rateel.app.core.model.AppResult
import com.rateel.app.data.local.AudioTrackDao
import com.rateel.app.data.local.MushafDao
import com.rateel.app.data.local.RadioDao
import com.rateel.app.data.local.RadioEntity
import com.rateel.app.data.local.RadioStreamEntity
import com.rateel.app.data.local.ReciterDao
import com.rateel.app.data.local.ReciterEntity
import com.rateel.app.data.remote.RadioRemoteDataSource
import com.rateel.app.data.remote.ReciterRemoteDataSource
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

private fun com.rateel.app.data.local.RadioWithStreams.toDomain(): RadioStation =
    RadioStation(
        id = station.id,
        nameArabic = station.nameArabic,
        nameEnglish = station.nameEnglish,
        description = station.description,
        streams = streams.sortedByDescending { it.isPrimary }.map {
            StreamEndpoint(
                url = it.url,
                format = it.format,
                bitrateKbps = it.bitrateKbps,
                primary = it.isPrimary,
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
) : RadioRepository {
    override fun observeRadios(): Flow<List<RadioStation>> =
        dao.observeAll().map { items -> items.map { it.toDomain() } }

    override suspend fun refresh(): AppResult<Unit> =
        when (val result = remote.fetchRadios()) {
            is AppResult.Success -> {
                val stations = result.data.map {
                    RadioEntity(
                        id = it.id,
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
                            id = station.id + ":" + index,
                            radioId = station.id,
                            url = stream.url,
                            format = stream.format,
                            bitrateKbps = stream.bitrateKbps,
                            isPrimary = stream.primary,
                        )
                    }
                }
                dao.replaceAll(stations, streams)
                AppResult.Success(Unit)
            }
            AppResult.Empty -> AppResult.Empty
            is AppResult.Error -> result
        }
}

@Singleton
class OfflineReciterRepository @Inject constructor(
    private val dao: ReciterDao,
    private val remote: ReciterRemoteDataSource,
) : ReciterRepository {
    override fun observeReciters(): Flow<List<Reciter>> =
        dao.observeAll().map { items ->
            items.map {
                Reciter(
                    id = it.id,
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
                            nameArabic = it.nameArabic,
                            nameEnglish = it.nameEnglish,
                            photoUrl = it.photoUrl,
                            country = it.country,
                            biography = it.biography,
                            featured = it.featured,
                        )
                    },
                )
                AppResult.Success(Unit)
            }
            AppResult.Empty -> AppResult.Empty
            is AppResult.Error -> result
        }
}

@Singleton
class LocalMushafRepository @Inject constructor(
    private val dao: MushafDao,
) : MushafRepository {
    override fun observeMushafs(reciterId: String): Flow<List<Mushaf>> =
        dao.observeByReciter(reciterId).map { items ->
            items.map {
                Mushaf(
                    id = it.id,
                    reciterId = it.reciterId,
                    name = it.name,
                    riwaya = it.riwaya,
                    description = it.description,
                    source = it.source,
                    quality = it.quality,
                    format = it.format,
                    totalSurahs = it.totalSurahs,
                    artworkUrl = it.artworkUrl,
                )
            }
        }
}

@Singleton
class LocalAudioRepository @Inject constructor(
    private val dao: AudioTrackDao,
) : AudioRepository {
    override fun observeTracks(mushafId: String): Flow<List<SurahAudio>> =
        dao.observeByMushaf(mushafId).map { items ->
            items.map {
                SurahAudio(
                    id = it.id,
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
}
