package com.rateel.app.feature.radio

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rateel.app.R
import com.rateel.app.core.model.AppResult
import com.rateel.app.core.network.NetworkStatusProvider
import com.rateel.app.domain.model.*
import com.rateel.app.domain.repository.RadioRepository
import com.rateel.app.domain.repository.SourceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class RadioItemUiModel(
    val id: String,
    val name: String,
    val sourceLabel: String,
    val category: String?,
    val isLive: Boolean,
    val health: StreamHealth,
    val streamUrl: String?,
    val capabilities: ContentCapabilities,
    val attribution: String?,
)

data class RadioUiState(
    val query: String = "",
    val items: List<RadioItemUiModel> = emptyList(),
    val loading: Boolean = false,
    val offline: Boolean = false,
    @StringRes val messageRes: Int? = null,
)

@HiltViewModel
class RadioViewModel @Inject constructor(
    private val repository: RadioRepository,
    sources: SourceRepository,
    network: NetworkStatusProvider,
) : ViewModel() {
    private val query = MutableStateFlow("")
    private val loading = MutableStateFlow(false)
    private val messageRes = MutableStateFlow<Int?>(null)

    val uiState: StateFlow<RadioUiState> = combine(
        repository.observeRadios(),
        sources.observeSources(),
        query,
        network.isOnline,
        loading,
        messageRes,
    ) { radios, sourceItems, search, online, busy, message ->
        val sourceMap = sourceItems.associateBy { it.id }
        val normalized = search.trim()
        val items = radios.mapNotNull { station ->
            val endpoint = station.streams.firstOrNull { it.primary } ?: station.streams.firstOrNull()
            val effectiveSource = endpoint?.sourceId?.let(sourceMap::get) ?: sourceMap[station.sourceId] ?: return@mapNotNull null
            val stationSource = sourceMap[station.sourceId]
            val endpointSource = endpoint?.sourceId?.let(sourceMap::get)
            val labels = listOfNotNull(stationSource?.name, endpointSource?.name?.takeIf { it != stationSource?.name }).distinct()
            val capabilities = ContentCapabilityResolver.resolve(
                effectiveSource,
                if (station.tags.contains("live-tv")) ContentType.LIVE_CHANNEL_AUDIO else ContentType.RADIO_STREAM,
                endpoint?.assetRightsStatus ?: AssetRightsStatus.INHERIT_SOURCE,
            )
            RadioItemUiModel(
                id = station.id,
                name = station.nameArabic,
                sourceLabel = labels.joinToString(" • ").ifBlank { effectiveSource.name },
                category = station.category,
                isLive = true,
                health = endpoint?.health ?: station.health,
                streamUrl = endpoint?.resolvedUrl ?: endpoint?.url,
                capabilities = capabilities,
                attribution = effectiveSource.attributionText?.takeIf { capabilities.requiresAttribution },
            )
        }.filter {
            normalized.isBlank() ||
                it.name.contains(normalized, ignoreCase = true) ||
                it.sourceLabel.contains(normalized, ignoreCase = true) ||
                it.category.orEmpty().contains(normalized, ignoreCase = true) ||
                radios.firstOrNull { radio -> radio.id == it.id }?.tags?.any { tag -> tag.contains(normalized, true) } == true
        }
        RadioUiState(normalized, items, busy, !online, message)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RadioUiState(loading = true))

    init { refresh(staleOnly = true) }

    fun setQuery(value: String) { query.value = value }
    fun retry() = refresh(staleOnly = false)

    private fun refresh(staleOnly: Boolean) {
        viewModelScope.launch {
            loading.value = true
            messageRes.value = null
            val result = runCatching {
                if (staleOnly) repository.refreshIfStale() else repository.refresh()
            }.getOrElse { AppResult.Error.Unknown(it) }
            messageRes.value = (result as? AppResult.Error)?.toRadioMessage()
            loading.value = false
        }
    }
}

private fun AppResult.Error.toRadioMessage(): Int = when (this) {
    AppResult.Error.Network -> R.string.error_network
    AppResult.Error.Server -> R.string.error_server
    AppResult.Error.Timeout -> R.string.error_timeout
    AppResult.Error.Parsing -> R.string.error_parsing
    is AppResult.Error.Unknown -> R.string.error_unknown
}
