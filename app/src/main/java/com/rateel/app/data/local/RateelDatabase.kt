package com.rateel.app.data.local
import androidx.room.*
import kotlinx.coroutines.flow.Flow
@Dao interface RadioDao{@Query("SELECT * FROM radio_stations ORDER BY isFeatured DESC, nameArabic") fun observeAll():Flow<List<RadioEntity>>;@Insert(onConflict=OnConflictStrategy.REPLACE) suspend fun upsertAll(items:List<RadioEntity>)}
@Dao interface ReciterDao{@Query("SELECT * FROM reciters ORDER BY featured DESC, nameArabic") fun observeAll():Flow<List<ReciterEntity>>;@Insert(onConflict=OnConflictStrategy.REPLACE) suspend fun upsertAll(items:List<ReciterEntity>)}
@Database(entities=[RadioEntity::class,RadioStreamEntity::class,ReciterEntity::class,MushafEntity::class,AudioTrackEntity::class,FavoriteEntity::class,ListeningHistoryEntity::class,DownloadEntity::class,PlaybackProgressEntity::class,CacheMetadataEntity::class,RecordingEntity::class],version=1,exportSchema=true)
abstract class RateelDatabase:RoomDatabase(){abstract fun radioDao():RadioDao;abstract fun reciterDao():ReciterDao}
