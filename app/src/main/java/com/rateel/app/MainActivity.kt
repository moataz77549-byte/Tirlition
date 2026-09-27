package com.rateel.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rateel.app.ui.navigation.RateelApp
import com.rateel.app.ui.theme.RateelTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val appViewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val themeMode by appViewModel.themeMode.collectAsStateWithLifecycle()
            RateelTheme(themeMode = themeMode) {
                RateelApp()
            }
        }
    }
}
