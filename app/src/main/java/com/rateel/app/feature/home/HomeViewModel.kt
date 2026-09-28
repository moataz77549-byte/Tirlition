package com.rateel.app.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rateel.app.R
import com.rateel.app.core.model.AppResult
import com.rateel.app.core.network.NetworkStatusProvider
import com.rateel.app.domain.repository.RadioRepository
import com.rateel.app.domain.repository.ReciterRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val radios: RadioRepository,
    private val reciters: ReciterRepository,
    network: NetworkStatusProvider,
) : ViewModel() {
    private val refreshing = MutableStateFlow(false)
    private val messageRes = MutableStateFlow<Int?>(null)

    val uiState = combine(radios.observeRadios(), reciters.observeReciters(), network.isOnline, refreshing, messageRes) { radioItems, reciterItems, online, loading, message ->
        val featuredRadios = radioItems.filter { it.isFeatured }.ifEmpty { radioItems }.take(6)
        val featuredReciters = reciterItems.filter { it.featured }.ifEmpty { reciterItems }.take(6)
        HomeUiState(loading, featuredRadios, featuredReciters, !online, message)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState(loading = true))

    init { refresh(staleOnly = true) }

    fun onAction(action: HomeAction) {
        when (action) {
            HomeAction.Retry -> refresh(staleOnly = false)
            is HomeAction.OpenRadio -> Unit
        }
    }

    private fun refresh(staleOnly: Boolean) {
        viewModelScope.launch {
            refreshing.value = true
            messageRes.value = null
            val radioResult = runCatching { if (staleOnly) radios.refreshIfStale() else radios.refresh() }.getOrElse { AppResult.Error.Unknown(it) }
            val reciterResult = runCatching { if (staleOnly) reciters.refreshIfStale() else reciters.refresh() }.getOrElse { AppResult.Error.Unknown(it) }
            messageRes.value = listOf(radioResult, reciterResult).filterIsInstance<AppResult.Error>().firstOrNull()?.toMessageRes()
            refreshing.value = false
        }
    }
}

private fun AppResult.Error.toMessageRes(): Int = when (this) {
    AppResult.Error.Network -> R.string.error_network
    AppResult.Error.Server -> R.string.error_server
    AppResult.Error.Timeout -> R.string.error_timeout
    AppResult.Error.Parsing -> R.string.error_parsing
    is AppResult.Error.Unknown -> R.string.error_unknown
}
