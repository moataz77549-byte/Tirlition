package com.rateel.app.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rateel.app.R
import com.rateel.app.data.settings.ThemeMode
import com.rateel.app.feature.home.HomeAction
import com.rateel.app.feature.home.HomeUiState
import com.rateel.app.feature.home.HomeViewModel
import com.rateel.app.feature.settings.SettingsViewModel

@Composable
fun HomeRoute(
    onSettings: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    HomeScreen(
        state = state,
        onSettings = onSettings,
        onRetry = { viewModel.onAction(HomeAction.Retry) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreen(
    state: HomeUiState,
    onSettings: () -> Unit,
    onRetry: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    IconButton(onClick = onSettings) {
                        Icon(Icons.Outlined.Settings, stringResource(R.string.settings))
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(
                    text = stringResource(R.string.home_headline),
                    style = MaterialTheme.typography.headlineMedium,
                )
            }

            if (state.offline) {
                item {
                    Card(Modifier.fillMaxWidth()) {
                        Text(
                            text = stringResource(R.string.offline_cached_content),
                            modifier = Modifier.padding(16.dp),
                        )
                    }
                }
            }

            state.messageRes?.let { message ->
                item {
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Text(stringResource(message))
                            TextButton(onClick = onRetry) {
                                Icon(Icons.Outlined.Refresh, contentDescription = null)
                                Text(stringResource(R.string.retry))
                            }
                        }
                    }
                }
            }

            if (state.loading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }

            if (!state.loading && state.featuredRadios.isEmpty() && state.featuredReciters.isEmpty()) {
                item {
                    Text(stringResource(R.string.home_empty))
                }
            }

            if (state.featuredRadios.isNotEmpty()) {
                item { Text(stringResource(R.string.featured_radios), style = MaterialTheme.typography.titleLarge) }
                items(state.featuredRadios, key = { it.id }) { radio ->
                    ListItem(
                        headlineContent = { Text(radio.nameArabic) },
                        supportingContent = { radio.category?.let { Text(it) } },
                    )
                }
            }

            if (state.featuredReciters.isNotEmpty()) {
                item { Text(stringResource(R.string.featured_reciters), style = MaterialTheme.typography.titleLarge) }
                items(state.featuredReciters, key = { it.id }) { reciter ->
                    ListItem(
                        headlineContent = { Text(reciter.nameArabic) },
                        supportingContent = { reciter.country?.let { Text(it) } },
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsRoute(
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val selected by viewModel.themeMode.collectAsStateWithLifecycle()
    SettingsScreen(selected = selected, onSelected = viewModel::setTheme)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsScreen(
    selected: ThemeMode,
    onSelected: (ThemeMode) -> Unit,
) {
    Scaffold(topBar = { TopAppBar(title = { Text(stringResource(R.string.settings)) }) }) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(stringResource(R.string.theme), style = MaterialTheme.typography.titleMedium)
            ThemeMode.entries.forEach { mode ->
                val label = when (mode) {
                    ThemeMode.SYSTEM -> R.string.theme_system
                    ThemeMode.LIGHT -> R.string.theme_light
                    ThemeMode.DARK -> R.string.theme_dark
                }
                ListItem(
                    headlineContent = { Text(stringResource(label)) },
                    leadingContent = {
                        RadioButton(
                            selected = selected == mode,
                            onClick = { onSelected(mode) },
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Placeholder(@StringRes title: Int) {
    Scaffold(topBar = { TopAppBar(title = { Text(stringResource(title)) }) }) { padding ->
        Box(
            Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.Center,
        ) {
            Text(stringResource(R.string.coming_soon))
        }
    }
}
