package com.rateel.app.playback

import android.content.Context
import com.rateel.app.data.local.RecordingDao
import com.rateel.app.domain.model.ContentAsset
import com.rateel.app.domain.model.ContentCapabilities
import com.rateel.app.domain.model.ContentCapabilityResolver
import com.rateel.app.domain.model.PlaybackItem
import com.rateel.app.domain.model.PlaybackType
import com.rateel.app.domain.model.SourceRightsStatus
import com.rateel.app.domain.model.toPlaybackItem
import com.rateel.app.domain.repository.AudioRepository
import com.rateel.app.domain.repository.RadioRepository
import com.rateel.app.domain.repository.SourceRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.net.URI
import javax.inject.Inject
import kotlinx.coroutines.flow.first

/** Resolve the last stable ID against current catalog metadata, never a serialized remote URL. */
class LastSessionResolver @Inject constructor(
    private val radios: RadioRepository,
    private val audio: AudioRepository,
    private val sources: SourceRepository,
    private val recordings: RecordingDao,
    @ApplicationContext private val context: Context,
) {
    suspend fun resolve(id: String): PlaybackItem? {
        if (id.startsWith("mp3quran:radio:") || id.startsWith("mp3quran:live:")) {
            radios.refreshIfStale()
            val station = radios.observeRadios().first().firstOrNull { it.id == id } ?: return null
            val endpoint = station.streams.firstOrNull() ?: return null
            val source = sources.getSource(endpoint.sourceId) ?: return null
            val host = runCatching { URI(endpoint.url).host?.lowercase() }.getOrNull()
            val capability = ContentCapabilityResolver.resolve(source,
                ContentAsset(endpoint.sourceId, host, station.sourceId,
                    station.category == "AUDIO_FROM_LIVE_CHANNEL"))
            return station.toPlaybackItem(endpoint, capability).takeIf { capability.canStream }
        }
        if (id.startsWith("mp3quran:mushaf:")) {
            val mushafId = id.substringBeforeLast(':', "")
            val track = audio.observeTracks(mushafId).first().firstOrNull { it.id == id } ?: return null
            val source = sources.getSource(track.sourceId) ?: return null
            val host = runCatching { URI(track.audioUrl).host?.lowercase() }.getOrNull()
            val capability = ContentCapabilityResolver.resolve(source,
                ContentAsset(source.id, host, source.id))
            return track.toPlaybackItem(null, null, capability).takeIf { capability.canStream }
        }
        val entry = recordings.get(id) ?: return null
        val directory = File(context.filesDir, "recordings").canonicalFile
        val file = runCatching { File(entry.filePath).canonicalFile }.getOrNull()
            ?.takeIf { it.parentFile == directory && it.isFile } ?: return null
        return PlaybackItem(id = entry.id, type = PlaybackType.LOCAL_RECORDING,
            title = entry.title, subtitle = entry.stationName, artwork = entry.artwork,
            sourceId = entry.sourceId, remoteUri = null, localUri = file.toURI().toString(),
            mimeType = entry.mimeType, isLive = false, durationMs = entry.durationMs,
            stationId = entry.stationId,
            capabilities = ContentCapabilities(true, false, false, true, false, false, null,
                SourceRightsStatus.STREAM_ONLY))
    }
}
