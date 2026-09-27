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
import kotlinx.coroutines.flow.Flow

data class RadioWithStreams(
    @Embedded val station: RadioEntity,
    @Relation(parentColumn = "id", entityColumn = "radioId")
    val streams: List<RadioStreamEntity>,
)

@Dao
abstract class RadioDao {
    @Transaction
    @Query("SELECT * FROM radio_stations ORDER BY isFeatured DESC, nameArabic")
    abstract fun observeAll(): Flow<List<RadioWithStreams>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun insertStations(items: List<RadioEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun insertStreams(items: List<RadioStreamEntity>)

    @Query("DELETE FROM radio_stations")
    protected abstract suspend fun deleteStations()

    @Transaction
    open suspend fun replaceAll(
        stations: List<RadioEntity>,
        streams: List<RadioStreamEntity>,
    ) {
        deleteStations()
        insertStations(stations)
        if (streams.isNotEmpty()) insertStreams(streams)
    }
}

@Dao
interface ReciterDao {
    @Query("SELECT * FROM reciters ORDER BY featured DESC, nameArabic")
    fun observeAll(): Flow<List<ReciterEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<ReciterEntity>)
}

@Dao
interface MushafDao {
    @Query("SELECT * FROM mushafs WHERE reciterId = :reciterId ORDER BY name")
    fun observeByReciter(reciterId: String): Flow<List<MushafEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<MushafEntity>)
}

@Dao
interface AudioTrackDao {
    @Query("SELECT * FROM audio_tracks WHERE mushafId = :mushafId ORDER BY surahNumber")
    fun observeByMushaf(mushafId: String): Flow<List<AudioTrackEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<AudioTrackEntity>)
}

@Database(
    entities = [
        RadioEntity::class,
        RadioStreamEntity::class,
        ReciterEntity::class,
        MushafEntity::class,
        AudioTrackEntity::class,
        FavoriteEntity::class,
        ListeningHistoryEntity::class,
        DownloadEntity::class,
        PlaybackProgressEntity::class,
        CacheMetadataEntity::class,
        RecordingEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class RateelDatabase : RoomDatabase() {
    abstract fun radioDao(): RadioDao
    abstract fun reciterDao(): ReciterDao
    abstract fun mushafDao(): MushafDao
    abstract fun audioTrackDao(): AudioTrackDao
}
