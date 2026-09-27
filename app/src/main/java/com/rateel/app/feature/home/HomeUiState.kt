package com.rateel.app.feature.home

import androidx.annotation.StringRes
import com.rateel.app.domain.model.RadioStation
import com.rateel.app.domain.model.Reciter

data class HomeUiState(
    val loading: Boolean = false,
    val featuredRadios: List<RadioStation> = emptyList(),
    val featuredReciters: List<Reciter> = emptyList(),
    val offline: Boolean = false,
    @StringRes val messageRes: Int? = null,
)

sealed interface HomeAction {
    data object Retry : HomeAction
    data class OpenRadio(val id: String) : HomeAction
}
