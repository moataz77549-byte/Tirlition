package com.rateel.app.feature.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rateel.app.core.model.AppResult
import com.rateel.app.data.provider.StreamValidation
import com.rateel.app.data.provider.StreamValidator
import com.rateel.app.domain.model.*
import com.rateel.app.domain.repository.*
import com.rateel.app.playback.PlaybackController
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
) : ViewModel() {
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
        playback.play(row.station.toPlaybackItem(endpoint, row.capabilities))
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
