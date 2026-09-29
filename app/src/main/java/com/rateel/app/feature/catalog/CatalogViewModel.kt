package com.rateel.app.feature.catalog

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rateel.app.core.model.AppResult
import com.rateel.app.data.local.FavoriteDao
import com.rateel.app.data.local.FavoriteEntity
import com.rateel.app.data.provider.StreamValidation
import com.rateel.app.data.provider.StreamValidator
import com.rateel.app.domain.model.*
import com.rateel.app.domain.repository.*
import com.rateel.app.playback.PlaybackController
import com.rateel.app.playback.RecordingService
import com.rateel.app.playback.StreamRecorder
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class RadioCatalogRow(val station: RadioStation, val capabilities: ContentCapabilities)

@HiltViewModel
class CatalogViewModel @Inject constructor(
    private val radios: RadioRepository,
    private val reciters: ReciterRepository,
    private val mushafs: MushafRepository,
    private val audio: AudioRepository,
    private val sources: SourceRepository,
    private val validator: StreamValidator,
    private val playback: PlaybackController,
    private val recorder: StreamRecorder,
    private val favoritesDao: FavoriteDao,
    @ApplicationContext private val context: Context,
) : ViewModel() {
    val recordingState = recorder.state
    val favoriteIds = favoritesDao.observeAll()
        .map { items -> items.filter { it.contentType == "radio" }.map { it.contentId }.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())
    val radioRows = combine(radios.observeRadios(), sources.observeSources()) { stations, rights ->
        val byId = rights.associateBy { it.id }
        stations.mapNotNull { station ->
            val endpoint = station.streams.firstOrNull() ?: return@mapNotNull null
            val source = byId[endpoint.sourceId] ?: return@mapNotNull null
            val host = runCatching { java.net.URI(endpoint.url).host?.lowercase() }.getOrNull()
            val capabilities = ContentCapabilityResolver.resolve(
                source,
                ContentAsset(endpoint.sourceId, host, station.sourceId,
                    station.category == "AUDIO_FROM_LIVE_CHANNEL"),
            )
            if (capabilities.canStream) RadioCatalogRow(station, capabilities) else null
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val reciterRows = reciters.observeReciters()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val error = MutableStateFlow<AppResult.Error?>(null)
    val refreshing = MutableStateFlow(false)
    val streamValidation = MutableStateFlow<StreamValidation?>(null)

    fun refreshCatalog(force: Boolean = false) = viewModelScope.launch {
        refreshing.value = true
        sources.ensureBuiltInCatalog()
        val radioResult = if (force) radios.refresh() else radios.refreshIfStale()
        val reciterResult = if (force) reciters.refresh() else reciters.refreshIfStale()
        error.value = (radioResult as? AppResult.Error) ?: (reciterResult as? AppResult.Error)
        refreshing.value = false
    }

    fun mushafs(reciterId: String): Flow<List<Mushaf>> = mushafs.observeMushafs(reciterId)
    fun tracks(mushafId: String): Flow<List<SurahAudio>> = audio.observeTracks(mushafId)

    fun refreshMushafs(reciterId: String) = viewModelScope.launch {
        sources.ensureBuiltInCatalog()
        error.value = mushafs.refresh(reciterId) as? AppResult.Error
    }
    fun refreshTracks(mushafId: String) = viewModelScope.launch {
        sources.ensureBuiltInCatalog()
        error.value = audio.refresh(mushafId) as? AppResult.Error
    }

    fun validateStream(url: String) = viewModelScope.launch {
        streamValidation.value = validator.check(url)
    }

    fun playRadio(id: String) {
        val row = radioRows.value.firstOrNull { it.station.id == id } ?: return
        val endpoint = row.station.streams.firstOrNull() ?: return
        if (!row.capabilities.canStream) return
        val queue = radioRows.value.mapNotNull { candidate ->
            candidate.station.streams.firstOrNull()?.let { stream ->
                candidate.station.toPlaybackItem(stream, candidate.capabilities)
            }
        }
        val index = queue.indexOfFirst { it.id == id }
        if (index >= 0) playback.playQueue(queue, index)
    }

    fun recordRadio(id: String, minutes: Int?) {
        val row = radioRows.value.firstOrNull { it.station.id == id } ?: return
        val endpoint = row.station.streams.firstOrNull() ?: return
        if (!row.capabilities.canRecord || (minutes != null && minutes !in listOf(5, 10, 15, 30))) return
        ContextCompat.startForegroundService(context, Intent(context, RecordingService::class.java).apply {
            action = RecordingService.ACTION_START
            putExtra(RecordingService.EXTRA_STATION, id)
            putExtra(RecordingService.EXTRA_NAME, row.station.nameArabic)
            putExtra(RecordingService.EXTRA_SOURCE, endpoint.sourceId)
            putExtra(RecordingService.EXTRA_URL, endpoint.url)
            putExtra(RecordingService.EXTRA_FORMAT, endpoint.format)
            putExtra(RecordingService.EXTRA_MINUTES, minutes ?: 0)
        })
    }

    fun stopRecording(cancel: Boolean = false) {
        context.startService(Intent(context, RecordingService::class.java).apply {
            action = if (cancel) RecordingService.ACTION_CANCEL else RecordingService.ACTION_STOP
        })
    }

    fun toggleRadioFavorite(id: String) = viewModelScope.launch {
        if (favoritesDao.get("radio", id) == null)
            favoritesDao.insert(FavoriteEntity(contentType = "radio", contentId = id,
                createdAt = System.currentTimeMillis()))
        else favoritesDao.delete("radio", id)
    }

    fun playSurah(id: String) = viewModelScope.launch {
        val mushafId = id.substringBeforeLast(':', "")
        val tracks = audio.observeTracks(mushafId).first().sortedBy { it.surahNumber }
        val start = tracks.indexOfFirst { it.id == id }
        if (start < 0) return@launch
        val source = sources.getSource(tracks[start].sourceId) ?: return@launch
        val host = runCatching { java.net.URI(tracks[start].audioUrl).host?.lowercase() }.getOrNull()
        val capabilities = ContentCapabilityResolver.resolve(
            source, ContentAsset(source.id, host, source.id),
        )
        if (!capabilities.canStream) return@launch
        playback.playQueue(tracks.map { it.toPlaybackItem(null, null, capabilities) }, start)
    }
}
