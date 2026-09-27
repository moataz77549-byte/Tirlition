package com.rateel.app.data.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("rateel_settings")

enum class ThemeMode { SYSTEM, LIGHT, DARK }

data class AppPreferences(
    val theme: ThemeMode = ThemeMode.SYSTEM,
    val language: String = "ar",
    val preferredAudioQuality: String = "auto",
    val wifiOnlyDownloads: Boolean = true,
    val autoResume: Boolean = true,
    val lastSelectedTab: String = "home",
    val lastPlaybackItemId: String? = null,
)

@Singleton
class AppSettings @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private object Keys {
        val theme = stringPreferencesKey("theme")
        val language = stringPreferencesKey("language")
        val preferredAudioQuality = stringPreferencesKey("preferred_audio_quality")
        val wifiOnlyDownloads = booleanPreferencesKey("wifi_only_downloads")
        val autoResume = booleanPreferencesKey("auto_resume")
        val lastSelectedTab = stringPreferencesKey("last_selected_tab")
        val lastPlaybackItemId = stringPreferencesKey("last_playback_item_id")
    }

    val preferences: Flow<AppPreferences> = context.dataStore.data.map { values ->
        AppPreferences(
            theme = runCatching {
                ThemeMode.valueOf(values[Keys.theme] ?: ThemeMode.SYSTEM.name)
            }.getOrDefault(ThemeMode.SYSTEM),
            language = values[Keys.language] ?: "ar",
            preferredAudioQuality = values[Keys.preferredAudioQuality] ?: "auto",
            wifiOnlyDownloads = values[Keys.wifiOnlyDownloads] ?: true,
            autoResume = values[Keys.autoResume] ?: true,
            lastSelectedTab = values[Keys.lastSelectedTab] ?: "home",
            lastPlaybackItemId = values[Keys.lastPlaybackItemId],
        )
    }.distinctUntilChanged()

    suspend fun setTheme(value: ThemeMode) {
        context.dataStore.edit { it[Keys.theme] = value.name }
    }

    suspend fun setLanguage(value: String) {
        context.dataStore.edit { it[Keys.language] = value }
    }

    suspend fun setPreferredAudioQuality(value: String) {
        context.dataStore.edit { it[Keys.preferredAudioQuality] = value }
    }

    suspend fun setWifiOnlyDownloads(value: Boolean) {
        context.dataStore.edit { it[Keys.wifiOnlyDownloads] = value }
    }

    suspend fun setAutoResume(value: Boolean) {
        context.dataStore.edit { it[Keys.autoResume] = value }
    }

    suspend fun setLastSelectedTab(value: String) {
        context.dataStore.edit { it[Keys.lastSelectedTab] = value }
    }

    suspend fun setLastPlaybackItemId(value: String?) {
        context.dataStore.edit { preferences ->
            if (value == null) preferences.remove(Keys.lastPlaybackItemId)
            else preferences[Keys.lastPlaybackItemId] = value
        }
    }
}
