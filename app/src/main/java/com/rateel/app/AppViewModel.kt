package com.rateel.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rateel.app.data.settings.AppSettings
import com.rateel.app.data.settings.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class AppViewModel @Inject constructor(
    settings: AppSettings,
) : ViewModel() {
    val themeMode: StateFlow<ThemeMode> = settings.preferences
        .map { it.theme }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ThemeMode.SYSTEM,
        )
}
