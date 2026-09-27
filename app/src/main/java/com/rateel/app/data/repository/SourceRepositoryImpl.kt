package com.rateel.app.data.repository

import com.rateel.app.data.local.ContentSourceEntity
import com.rateel.app.data.local.SourceDao
import com.rateel.app.domain.model.ContentSource
import com.rateel.app.domain.model.LicenseType
import com.rateel.app.domain.model.SourceType
import com.rateel.app.domain.repository.SourceRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private fun ContentSourceEntity.toDomain() = ContentSource(
    id = id,
    name = name,
    provider = provider,
    type = runCatching { SourceType.valueOf(type) }.getOrDefault(SourceType.OTHER),
    website = website,
    apiBaseUrl = apiBaseUrl,
    documentationUrl = documentationUrl,
    termsUrl = termsUrl,
    copyrightUrl = copyrightUrl,
    attributionText = attributionText,
    licenseType = runCatching { LicenseType.valueOf(licenseType) }.getOrDefault(LicenseType.UNKNOWN),
    requiresAttribution = requiresAttribution,
    allowStreaming = allowStreaming,
    allowDownload = allowDownload,
    allowOfflinePlayback = allowOfflinePlayback,
    allowRecording = allowRecording,
    allowSharing = allowSharing,
    allowCommercialUse = allowCommercialUse,
    maxOfflineRetentionDays = maxOfflineRetentionDays,
    requiresPeriodicSync = requiresPeriodicSync,
    isOfficial = isOfficial,
    isVerified = isVerified,
    lastRightsCheckAt = lastRightsCheckAt,
    lastTechnicalCheckAt = lastTechnicalCheckAt,
    notes = notes,
    isEnabled = isEnabled,
    streamingEnabled = streamingEnabled,
    downloadEnabled = downloadEnabled,
    offlinePlaybackEnabled = offlinePlaybackEnabled,
    recordingEnabled = recordingEnabled,
    sharingEnabled = sharingEnabled,
    disabledReason = disabledReason,
)

@Singleton
class LocalSourceRepository @Inject constructor(
    private val dao: SourceDao,
) : SourceRepository {
    override fun observeSources(): Flow<List<ContentSource>> =
        dao.observeEnabled().map { items -> items.map { it.toDomain() } }

    override suspend fun getSource(id: String): ContentSource? =
        dao.getById(id)?.toDomain()
}
