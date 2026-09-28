package com.rateel.app.data.repository

import com.rateel.app.data.local.ContentSourceEntity
import com.rateel.app.data.local.SourceDao
import com.rateel.app.domain.model.*
import com.rateel.app.domain.repository.SourceRepository
import com.rateel.app.domain.source.SourceRegistry
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private fun ContentSourceEntity.toDomain() = ContentSource(
    id = id, name = name, provider = provider,
    type = runCatching { SourceType.valueOf(type) }.getOrDefault(SourceType.OTHER),
    website = website, apiBaseUrl = apiBaseUrl, documentationUrl = documentationUrl,
    termsUrl = termsUrl, copyrightUrl = copyrightUrl, attributionText = attributionText,
    licenseType = runCatching { LicenseType.valueOf(licenseType) }.getOrDefault(LicenseType.UNKNOWN),
    requiresAttribution = requiresAttribution, allowStreaming = allowStreaming, allowDownload = allowDownload,
    allowOfflinePlayback = allowOfflinePlayback, allowCaching = allowCaching, allowOfflineSync = allowOfflineSync,
    allowRecording = allowRecording, allowSharing = allowSharing, allowCommercialUse = allowCommercialUse,
    maxOfflineRetentionDays = maxOfflineRetentionDays, requiresPeriodicSync = requiresPeriodicSync,
    isOfficial = isOfficial, isVerified = isVerified, lastRightsCheckAt = lastRightsCheckAt,
    lastTechnicalCheckAt = lastTechnicalCheckAt, notes = notes, isEnabled = isEnabled,
    streamingEnabled = streamingEnabled, downloadEnabled = downloadEnabled,
    offlinePlaybackEnabled = offlinePlaybackEnabled, cachingEnabled = cachingEnabled,
    offlineSyncEnabled = offlineSyncEnabled, recordingEnabled = recordingEnabled,
    sharingEnabled = sharingEnabled, disabledReason = disabledReason,
).let { stored ->
    SourceRegistry.byId(stored.id)?.copy(
        isEnabled = stored.isEnabled,
        streamingEnabled = stored.streamingEnabled,
        downloadEnabled = stored.downloadEnabled,
        offlinePlaybackEnabled = stored.offlinePlaybackEnabled,
        cachingEnabled = stored.cachingEnabled,
        offlineSyncEnabled = stored.offlineSyncEnabled,
        recordingEnabled = stored.recordingEnabled,
        sharingEnabled = stored.sharingEnabled,
        disabledReason = stored.disabledReason,
    ) ?: stored
}

private fun ContentSource.toEntity() = ContentSourceEntity(
    id, name, provider, type.name, website, apiBaseUrl, documentationUrl, termsUrl, copyrightUrl,
    attributionText, licenseType.name, requiresAttribution, allowStreaming, allowDownload,
    allowOfflinePlayback, allowCaching, allowOfflineSync, allowRecording, allowSharing,
    allowCommercialUse, maxOfflineRetentionDays, requiresPeriodicSync, isOfficial, isVerified,
    lastRightsCheckAt, lastTechnicalCheckAt, notes, isEnabled, streamingEnabled, downloadEnabled,
    offlinePlaybackEnabled, cachingEnabled, offlineSyncEnabled, recordingEnabled, sharingEnabled, disabledReason,
)

@Singleton
class LocalSourceRepository @Inject constructor(private val dao: SourceDao) : SourceRepository {
    override fun observeSources(): Flow<List<ContentSource>> = dao.observeAll().map { rows -> rows.map { it.toDomain() } }
    override suspend fun getSource(id: String): ContentSource? = dao.getById(id)?.toDomain()
    override suspend fun ensureBuiltInCatalog() { dao.insertIfMissing(SourceRegistry.all.map { it.toEntity() }) }
}
