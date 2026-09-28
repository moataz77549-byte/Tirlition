package com.rateel.app.feature.reciters

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rateel.app.R
import com.rateel.app.core.model.AppResult
import com.rateel.app.core.network.NetworkStatusProvider
import com.rateel.app.domain.model.*
import com.rateel.app.domain.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class RecitersUiState(
    val query: String = "",
    val items: List<Reciter> = emptyList(),
    val loading: Boolean = false,
    val offline: Boolean = false,
    @StringRes val messageRes: Int? = null,
)

@HiltViewModel
class RecitersViewModel @Inject constructor(
    private val repository: ReciterRepository,
    network: NetworkStatusProvider,
) : ViewModel() {
    private val query = MutableStateFlow("")
    private val loading = MutableStateFlow(false)
    private val messageRes = MutableStateFlow<Int?>(null)

    val uiState = combine(repository.observeReciters(), query, network.isOnline, loading, messageRes) { reciters, search, online, busy, message ->
        val q = search.trim()
        RecitersUiState(
            query = q,
            items = reciters.filter { q.isBlank() || it.nameArabic.contains(q, true) || it.nameEnglish.orEmpty().contains(q, true) },
            loading = busy,
            offline = !online,
            messageRes = message,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RecitersUiState(loading = true))

    init { refresh(true) }
    fun setQuery(value: String) { query.value = value }
    fun retry() = refresh(false)

    private fun refresh(staleOnly: Boolean) {
        viewModelScope.launch {
            loading.value = true
            messageRes.value = null
            val result = runCatching { if (staleOnly) repository.refreshIfStale() else repository.refresh() }
                .getOrElse { AppResult.Error.Unknown(it) }
            messageRes.value = (result as? AppResult.Error)?.toMessage()
            loading.value = false
        }
    }
}

data class MushafItemUiModel(
    val mushaf: Mushaf,
    val availableCount: Int,
    val isComplete: Boolean,
    val sourceLabel: String,
)

@HiltViewModel
class ReciterDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    mushafs: MushafRepository,
    sources: SourceRepository,
) : ViewModel() {
    val reciterId: String = checkNotNull(savedStateHandle["reciterId"])
    val items: StateFlow<List<MushafItemUiModel>> = combine(
        mushafs.observeMushafs(reciterId),
        sources.observeSources(),
    ) { rows, sourceItems ->
        val sourceMap = sourceItems.associateBy { it.id }
        rows.map {
            MushafItemUiModel(
                mushaf = it,
                availableCount = it.availableSurahs.size,
                isComplete = it.availableSurahs.size == 114,
                sourceLabel = sourceMap[it.sourceId]?.name ?: it.sourceId,
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}

data class SurahAudioUiModel(
    val track: SurahAudio,
    val sourceLabel: String,
    val capabilities: ContentCapabilities,
)

@HiltViewModel
class MushafViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    audio: AudioRepository,
    sources: SourceRepository,
) : ViewModel() {
    val mushafId: String = checkNotNull(savedStateHandle["mushafId"])
    val tracks: StateFlow<List<SurahAudioUiModel>> = combine(
        audio.observeTracks(mushafId),
        sources.observeSources(),
    ) { rows, sourceItems ->
        val sourceMap = sourceItems.associateBy { it.id }
        rows.mapNotNull { track ->
            val source = sourceMap[track.sourceId] ?: return@mapNotNull null
            SurahAudioUiModel(
                track,
                source.name,
                ContentCapabilityResolver.resolve(source, ContentType.SURAH_AUDIO, track.assetRightsStatus),
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}

private fun AppResult.Error.toMessage(): Int = when (this) {
    AppResult.Error.Network -> R.string.error_network
    AppResult.Error.Server -> R.string.error_server
    AppResult.Error.Timeout -> R.string.error_timeout
    AppResult.Error.Parsing -> R.string.error_parsing
    is AppResult.Error.Unknown -> R.string.error_unknown
}
