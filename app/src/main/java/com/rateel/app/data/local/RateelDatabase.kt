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
    version = 3,
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
    abstract fun playbackProgressDao(): PlaybackProgressDao
    abstract fun listeningHistoryDao(): ListeningHistoryDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun recordingDao(): RecordingDao
}

@Dao
interface PlaybackProgressDao {
    @Query("SELECT * FROM playback_progress WHERE contentId = :id LIMIT 1")
    suspend fun get(id: String): PlaybackProgressEntity?
    @Upsert suspend fun upsert(item: PlaybackProgressEntity)
}

@Dao
interface ListeningHistoryDao {
    @Query("SELECT * FROM listening_history WHERE contentType = :type AND contentId = :id ORDER BY playedAt DESC LIMIT 1")
    suspend fun latest(type: String, id: String): ListeningHistoryEntity?
    @Query("SELECT * FROM listening_history ORDER BY playedAt DESC LIMIT 100")
    fun observeRecent(): Flow<List<ListeningHistoryEntity>>
    @Upsert suspend fun upsert(item: ListeningHistoryEntity)
}

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<FavoriteEntity>>
    @Query("SELECT * FROM favorites WHERE contentType = :type AND contentId = :id LIMIT 1")
    suspend fun get(type: String, id: String): FavoriteEntity?
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insert(item: FavoriteEntity)
    @Query("DELETE FROM favorites WHERE contentType = :type AND contentId = :id")
    suspend fun delete(type: String, id: String)
}

@Dao
interface RecordingDao {
    @Query("SELECT * FROM recordings ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<LocalRecordingEntity>>
    @Query("SELECT * FROM recordings WHERE id = :id LIMIT 1")
    suspend fun get(id: String): LocalRecordingEntity?
    @Insert(onConflict = OnConflictStrategy.ABORT) suspend fun insert(item: LocalRecordingEntity)
    @Query("UPDATE recordings SET title = :title WHERE id = :id")
    suspend fun rename(id: String, title: String)
    @Query("DELETE FROM recordings WHERE id = :id")
    suspend fun delete(id: String)
}
