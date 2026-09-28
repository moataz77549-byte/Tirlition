package com.rateel.app.data.local

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Relation
import androidx.room.RoomDatabase
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

data class RadioWithStreams(
    @Embedded val station: RadioEntity,
    @Relation(parentColumn = "id", entityColumn = "radioId") val streams: List<RadioStreamEntity>,
)

@Dao
interface SourceDao {
    @Query("SELECT * FROM content_sources ORDER BY name")
    fun observeAll(): Flow<List<ContentSourceEntity>>

    @Query("SELECT * FROM content_sources WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): ContentSourceEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfMissing(items: List<ContentSourceEntity>)

    @Upsert
    suspend fun upsertAll(items: List<ContentSourceEntity>)
}

@Dao
abstract class RadioDao {
    @Transaction
    @Query("SELECT * FROM radio_stations ORDER BY isFeatured DESC, nameArabic")
    abstract fun observeAll(): Flow<List<RadioWithStreams>>

    @Upsert
    protected abstract suspend fun insertStations(items: List<RadioEntity>)

    @Upsert
    protected abstract suspend fun insertStreams(items: List<RadioStreamEntity>)

    @Transaction
    open suspend fun replaceAll(stations: List<RadioEntity>, streams: List<RadioStreamEntity>) {
        insertStations(stations)
        if (streams.isNotEmpty()) insertStreams(streams)
    }
}

@Dao
interface ReciterDao {
    @Query("SELECT * FROM reciters ORDER BY featured DESC, nameArabic")
    fun observeAll(): Flow<List<ReciterEntity>>
    @Upsert
    suspend fun upsertAll(items: List<ReciterEntity>)
}

@Dao
interface MushafDao {
    @Query("SELECT * FROM mushafs WHERE reciterId = :reciterId ORDER BY name")
    fun observeByReciter(reciterId: String): Flow<List<MushafEntity>>
    @Upsert
    suspend fun upsertAll(items: List<MushafEntity>)
}

@Dao
interface AudioTrackDao {
    @Query("SELECT * FROM audio_tracks WHERE mushafId = :mushafId ORDER BY surahNumber")
    fun observeByMushaf(mushafId: String): Flow<List<AudioTrackEntity>>
    @Upsert
    suspend fun upsertAll(items: List<AudioTrackEntity>)
}

@Dao
interface SurahMetadataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<SurahMetadataEntity>)
}

@Dao
interface CacheMetadataDao {
    @Query("SELECT * FROM cache_metadata WHERE `key` = :key LIMIT 1")
    suspend fun get(key: String): CacheMetadataEntity?

    @Upsert
    suspend fun upsert(item: CacheMetadataEntity)
}

@Database(
    entities = [
        ContentSourceEntity::class,
        RadioEntity::class,
        RadioStreamEntity::class,
        ReciterEntity::class,
        MushafEntity::class,
        AudioTrackEntity::class,
        SurahMetadataEntity::class,
        FavoriteEntity::class,
        ListeningHistoryEntity::class,
        DownloadEntity::class,
        PlaybackProgressEntity::class,
        CacheMetadataEntity::class,
        LocalRecordingEntity::class,
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
    abstract fun surahMetadataDao(): SurahMetadataDao
    abstract fun cacheMetadataDao(): CacheMetadataDao
}
