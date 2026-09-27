package com.rateel.app.data.settings
import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map
private val Context.dataStore by preferencesDataStore("rateel_settings")
enum class ThemeMode{SYSTEM,LIGHT,DARK}
class AppSettings(private val context:Context){private val themeKey=stringPreferencesKey("theme");private val tabKey=stringPreferencesKey("last_tab");val theme=context.dataStore.data.map{runCatching{ThemeMode.valueOf(it[themeKey]?:"SYSTEM")}.getOrDefault(ThemeMode.SYSTEM)};val lastTab=context.dataStore.data.map{it[tabKey]?:"home"};suspend fun setTheme(v:ThemeMode)=context.dataStore.edit{it[themeKey]=v.name};suspend fun setLastTab(v:String)=context.dataStore.edit{it[tabKey]=v}}
