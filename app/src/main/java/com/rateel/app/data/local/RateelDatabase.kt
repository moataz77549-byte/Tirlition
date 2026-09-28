package com.rateel.app.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

data class RadioWithStreams(
    @Embedded val station: RadioEntity,
    @Relation(parentColumn = "id", entityColumn = "radioId") val streams: List<RadioStreamEntity>,
)

data class MushafWithTracks(
    @Embedded val mushaf: MushafEntity,
    @Relation(parentColumn = "id", entityColumn = "mushafId") val tracks: List<AudioTrackEntity>,
)

@Dao
interface SourceDao {
    @Query("SELECT * FROM content_sources ORDER BY name") fun observeAll(): Flow<List<ContentSourceEntity>>
    @Query("SELECT * FROM content_sources WHERE id = :id LIMIT 1") suspend fun getById(id: String): ContentSourceEntity?
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insertIfMissing(items: List<ContentSourceEntity>)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertAll(items: List<ContentSourceEntity>)
}

@Dao
abstract class RadioDao {
    @Transaction @Query("SELECT * FROM radio_stations ORDER BY isFeatured DESC, nameArabic")
    abstract fun observeAll(): Flow<List<RadioWithStreams>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) protected abstract suspend fun insertStations(items: List<RadioEntity>)
    @Insert(onConflict = OnConflictStrategy.REPLACE) protected abstract suspend fun insertStreams(items: List<RadioStreamEntity>)
    @Query("DELETE FROM radio_stations WHERE sourceId = :sourceId") protected abstract suspend fun deleteBySource(sourceId: String)

    @Transaction
    open suspend fun replaceBySource(sourceId: String, stations: List<RadioEntity>, streams: List<RadioStreamEntity>) {
        deleteBySource(sourceId)
        if (stations.isNotEmpty()) insertStations(stations)
        if (streams.isNotEmpty()) insertStreams(streams)
    }
}

@Dao
interface ReciterDao {
    @Query("SELECT * FROM reciters ORDER BY featured DESC, nameArabic") fun observeAll(): Flow<List<ReciterEntity>>
}

@Dao
interface MushafDao {
    @Transaction
    @Query("SELECT * FROM mushafs WHERE reciterId = :reciterId ORDER BY name")
    fun observeByReciter(reciterId: String): Flow<List<MushafWithTracks>>
}

@Dao
interface AudioTrackDao {
    @Query("SELECT * FROM audio_tracks WHERE mushafId = :mushafId ORDER BY surahNumber")
    fun observeByMushaf(mushafId: String): Flow<List<AudioTrackEntity>>
}

@Dao
abstract class CatalogDao {
    @Query("DELETE FROM reciters WHERE sourceId = :sourceId") protected abstract suspend fun deleteReciters(sourceId: String)
    @Query("DELETE FROM quran_languages") protected abstract suspend fun deleteLanguages()
    @Query("DELETE FROM quran_surahs") protected abstract suspend fun deleteSurahs()
    @Query("DELETE FROM riwayat") protected abstract suspend fun deleteRiwayat()
    @Insert(onConflict = OnConflictStrategy.REPLACE) protected abstract suspend fun insertReciters(items: List<ReciterEntity>)
    @Insert(onConflict = OnConflictStrategy.REPLACE) protected abstract suspend fun insertMushafs(items: List<MushafEntity>)
    @Insert(onConflict = OnConflictStrategy.REPLACE) protected abstract suspend fun insertTracks(items: List<AudioTrackEntity>)
    @Insert(onConflict = OnConflictStrategy.REPLACE) protected abstract suspend fun insertLanguages(items: List<QuranLanguageEntity>)
    @Insert(onConflict = OnConflictStrategy.REPLACE) protected abstract suspend fun insertSurahs(items: List<QuranSurahEntity>)
    @Insert(onConflict = OnConflictStrategy.REPLACE) protected abstract suspend fun insertRiwayat(items: List<RiwayaEntity>)

    @Transaction
    open suspend fun replaceMp3QuranCatalog(
        sourceId: String,
        reciters: List<ReciterEntity>,
        mushafs: List<MushafEntity>,
        tracks: List<AudioTrackEntity>,
        languages: List<QuranLanguageEntity>,
        surahs: List<QuranSurahEntity>,
        riwayat: List<RiwayaEntity>,
    ) {
        deleteReciters(sourceId)
        deleteLanguages()
        deleteSurahs()
        deleteRiwayat()
        if (reciters.isNotEmpty()) insertReciters(reciters)
        if (mushafs.isNotEmpty()) insertMushafs(mushafs)
        if (tracks.isNotEmpty()) insertTracks(tracks)
        if (languages.isNotEmpty()) insertLanguages(languages)
        if (surahs.isNotEmpty()) insertSurahs(surahs)
        if (riwayat.isNotEmpty()) insertRiwayat(riwayat)
    }
}

@Dao
interface SourceSyncDao {
    @Query("SELECT * FROM source_sync WHERE syncKey = :syncKey LIMIT 1") suspend fun get(syncKey: String): SourceSyncEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(item: SourceSyncEntity)
}

@Database(
    entities = [
        ContentSourceEntity::class, RadioEntity::class, RadioStreamEntity::class,
        ReciterEntity::class, MushafEntity::class, AudioTrackEntity::class,
        QuranLanguageEntity::class, QuranSurahEntity::class, RiwayaEntity::class, SourceSyncEntity::class,
        FavoriteEntity::class, ListeningHistoryEntity::class, DownloadEntity::class,
        PlaybackProgressEntity::class, CacheMetadataEntity::class, LocalRecordingEntity::class,
    ],
    version = 2,
    exportSchema = true,
)
abstract class RateelDatabase : RoomDatabase() {
    abstract fun sourceDao(): SourceDao
    abstract fun radioDao(): RadioDao
    abstract fun reciterDao(): ReciterDao
    abstract fun mushafDao(): MushafDao
    abstract fun audioTrackDao(): AudioTrackDao
    abstract fun catalogDao(): CatalogDao
    abstract fun sourceSyncDao(): SourceSyncDao
}
