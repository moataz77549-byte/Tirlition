package com.rateel.app.data.local

import androidx.room.Entity
import androidx.room.ColumnInfo
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "content_sources")
data class ContentSourceEntity(
    @PrimaryKey val id: String,
    val name: String,
    val provider: String,
    val type: String,
    val website: String?,
    val apiBaseUrl: String?,
    val documentationUrl: String?,
    val termsUrl: String?,
    val copyrightUrl: String?,
    val attributionText: String?,
    val licenseType: String,
    val requiresAttribution: Boolean,
    val allowStreaming: Boolean,
    val allowDownload: Boolean,
    val allowOfflinePlayback: Boolean,
    val allowCaching: Boolean,
    val allowOfflineSync: Boolean,
    val allowRecording: Boolean,
    val allowSharing: Boolean,
    val allowCommercialUse: Boolean,
    val maxOfflineRetentionDays: Int?,
    val requiresPeriodicSync: Boolean,
    val isOfficial: Boolean,
    val isVerified: Boolean,
    val lastRightsCheckAt: Long?,
    val lastTechnicalCheckAt: Long?,
    val notes: String?,
    val isEnabled: Boolean,
    val streamingEnabled: Boolean,
    val downloadEnabled: Boolean,
    val offlinePlaybackEnabled: Boolean,
    val cachingEnabled: Boolean,
    val offlineSyncEnabled: Boolean,
    val recordingEnabled: Boolean,
    val sharingEnabled: Boolean,
    val disabledReason: String?,
)

@Entity(
    tableName = "radio_stations",
    foreignKeys = [ForeignKey(entity = ContentSourceEntity::class, parentColumns = ["id"], childColumns = ["sourceId"], onDelete = ForeignKey.NO_ACTION)],
    indices = [Index("sourceId"), Index(value = ["sourceId", "canonicalKey"], unique = true)],
)
data class RadioEntity(
    @PrimaryKey val id: String,
    val sourceId: String,
    val canonicalKey: String,
    val nameArabic: String,
    val nameEnglish: String?,
    val description: String?,
    val logoUrl: String?,
    val country: String?,
    val language: String,
    val category: String?,
    val website: String?,
    val isActive: Boolean,
    val isFeatured: Boolean,
    val isVerified: Boolean,
    val health: String,
    val createdAt: Long?,
    val updatedAt: Long?,
)

@Entity(
    tableName = "radio_streams",
    foreignKeys = [
        ForeignKey(entity = RadioEntity::class, parentColumns = ["id"], childColumns = ["radioId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = ContentSourceEntity::class, parentColumns = ["id"], childColumns = ["sourceId"], onDelete = ForeignKey.NO_ACTION),
    ],
    indices = [Index("radioId"), Index("sourceId")],
)
data class RadioStreamEntity(
    @PrimaryKey val id: String,
    val radioId: String,
    val sourceId: String,
    val providerEndpointId: String?,
    val url: String,
    val format: String?,
    val bitrateKbps: Int?,
    val isPrimary: Boolean,
)

@Entity(
    tableName = "reciters",
    foreignKeys = [ForeignKey(entity = ContentSourceEntity::class, parentColumns = ["id"], childColumns = ["sourceId"], onDelete = ForeignKey.NO_ACTION)],
    indices = [Index("sourceId")],
)
data class ReciterEntity(
    @PrimaryKey val id: String,
    val sourceId: String,
    val nameArabic: String,
    val nameEnglish: String?,
    val photoUrl: String?,
    val country: String?,
    val biography: String?,
    val featured: Boolean,
)

@Entity(
    tableName = "mushafs",
    foreignKeys = [
        ForeignKey(entity = ReciterEntity::class, parentColumns = ["id"], childColumns = ["reciterId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = ContentSourceEntity::class, parentColumns = ["id"], childColumns = ["sourceId"], onDelete = ForeignKey.NO_ACTION),
    ],
    indices = [Index("reciterId"), Index("sourceId")],
)
data class MushafEntity(
    @PrimaryKey val id: String,
    val sourceId: String,
    val reciterId: String,
    val name: String,
    val riwaya: String,
    val description: String?,
    val source: String?,
    val quality: String?,
    val format: String?,
    val totalSurahs: Int,
    val artworkUrl: String?,
    @ColumnInfo(defaultValue = "''") val availableSurahs: String = "",
)

@Entity(
    tableName = "audio_tracks",
    foreignKeys = [
        ForeignKey(entity = MushafEntity::class, parentColumns = ["id"], childColumns = ["mushafId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = ContentSourceEntity::class, parentColumns = ["id"], childColumns = ["sourceId"], onDelete = ForeignKey.NO_ACTION),
    ],
    indices = [Index("mushafId"), Index("sourceId"), Index("surahNumber")],
)
data class AudioTrackEntity(
    @PrimaryKey val id: String,
    val sourceId: String,
    val mushafId: String,
    val surahNumber: Int,
    val surahNameArabic: String,
    val surahNameEnglish: String?,
    val audioUrl: String,
    val durationMs: Long?,
    val fileSizeBytes: Long?,
    val format: String?,
    val bitrateKbps: Int?,
    val quality: String?,
    val checksum: String?,
    val downloadable: Boolean,
)

@Entity(tableName = "surah_metadata")
data class SurahMetadataEntity(
    @PrimaryKey val number: Int,
    val name: String,
    val startPage: Int?,
    val endPage: Int?,
    val isMakki: Boolean?,
)

@Entity(tableName = "favorites", indices = [Index(value = ["contentType", "contentId"], unique = true)])
data class FavoriteEntity(@PrimaryKey(autoGenerate = true) val id: Long = 0, val contentType: String, val contentId: String, val createdAt: Long)

@Entity(tableName = "listening_history", indices = [Index("contentId")])
data class ListeningHistoryEntity(@PrimaryKey(autoGenerate = true) val id: Long = 0, val contentType: String, val contentId: String, val playedAt: Long, val positionMs: Long = 0)

@Entity(
    tableName = "downloads",
    foreignKeys = [ForeignKey(entity = ContentSourceEntity::class, parentColumns = ["id"], childColumns = ["sourceId"], onDelete = ForeignKey.NO_ACTION)],
    indices = [Index("sourceId"), Index("mushafId"), Index("surahNumber")],
)
data class DownloadEntity(
    @PrimaryKey val id: String,
    val sourceId: String,
    val remoteUrl: String,
    val localUri: String?,
    val contentType: String,
    val reciterId: String?,
    val mushafId: String?,
    val surahNumber: Int?,
    val fileSizeBytes: Long?,
    val checksum: String?,
    val downloadedAt: Long?,
    val rightsSnapshot: String,
    val status: String,
    val bytesDownloaded: Long = 0,
    val totalBytes: Long? = null,
    val updatedAt: Long,
)

@Entity(tableName = "playback_progress")
data class PlaybackProgressEntity(@PrimaryKey val contentId: String, val positionMs: Long, val durationMs: Long?, val updatedAt: Long)

@Entity(tableName = "cache_metadata")
data class CacheMetadataEntity(@PrimaryKey val key: String, val updatedAt: Long, val expiresAt: Long?)

@Entity(
    tableName = "recordings",
    foreignKeys = [
        ForeignKey(entity = RadioEntity::class, parentColumns = ["id"], childColumns = ["stationId"], onDelete = ForeignKey.NO_ACTION),
        ForeignKey(entity = ContentSourceEntity::class, parentColumns = ["id"], childColumns = ["sourceId"], onDelete = ForeignKey.NO_ACTION),
    ],
    indices = [Index("stationId"), Index("sourceId")],
)
data class LocalRecordingEntity(
    @PrimaryKey val id: String,
    val stationId: String,
    val sourceId: String,
    val stationName: String,
    val title: String,
    val filePath: String,
    val mimeType: String,
    val durationMs: Long?,
    val fileSizeBytes: Long?,
    val startedAt: Long,
    val finishedAt: Long?,
    val recordingMode: String,
    val requestedDurationMs: Long?,
    val artwork: String?,
    val sourceAttribution: String?,
    val createdAt: Long,
)
