package com.rateel.app
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.rateel.app.ui.navigation.RateelApp
import com.rateel.app.ui.theme.RateelTheme
import dagger.hilt.android.AndroidEntryPoint
@AndroidEntryPoint class MainActivity:ComponentActivity(){override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);setContent{RateelTheme{RateelApp()}}}}
