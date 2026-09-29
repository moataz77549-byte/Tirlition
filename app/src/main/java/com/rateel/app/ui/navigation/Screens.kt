package com.rateel.app.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
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
import com.rateel.app.BuildConfig
import com.rateel.app.data.settings.ThemeMode
import com.rateel.app.domain.model.ContentSource
import com.rateel.app.feature.home.HomeAction
import com.rateel.app.feature.home.HomeUiState
import com.rateel.app.feature.home.HomeViewModel
import com.rateel.app.feature.settings.SettingsViewModel
import com.rateel.app.feature.sources.SourcesViewModel

@Composable
fun HomeRoute(
    onSettings: () -> Unit,
    onRadio: (String) -> Unit,
    onReciter: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    HomeScreen(
        state = state,
        onSettings = onSettings,
        onRadio = onRadio,
        onReciter = onReciter,
        onRetry = { viewModel.onAction(HomeAction.Retry) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreen(
    state: HomeUiState,
    onSettings: () -> Unit,
    onRadio: (String) -> Unit,
    onReciter: (String) -> Unit,
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
            modifier = Modifier.fillMaxSize().padding(padding),
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
                item { Text(stringResource(R.string.home_empty)) }
            }
            if (state.featuredRadios.isNotEmpty()) {
                item { Text(stringResource(R.string.featured_radios), style = MaterialTheme.typography.titleLarge) }
                items(state.featuredRadios, key = { it.id }) { radio ->
                    ListItem(
                        headlineContent = { Text(radio.nameArabic) },
                        supportingContent = { radio.category?.let { Text(it) } },
                        modifier = Modifier.fillMaxWidth().clickable { onRadio(radio.id) },
                    )
                }
            }
            if (state.featuredReciters.isNotEmpty()) {
                item { Text(stringResource(R.string.featured_reciters), style = MaterialTheme.typography.titleLarge) }
                items(state.featuredReciters, key = { it.id }) { reciter ->
                    ListItem(
                        headlineContent = { Text(reciter.nameArabic) },
                        supportingContent = { reciter.country?.let { Text(it) } },
                        modifier = Modifier.fillMaxWidth().clickable { onReciter(reciter.id) },
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsRoute(
    onSources: () -> Unit,
    onAbout: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val selected by viewModel.themeMode.collectAsStateWithLifecycle()
    SettingsScreen(
        selected = selected,
        onSelected = viewModel::setTheme,
        onSources = onSources,
        onAbout = onAbout,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsScreen(
    selected: ThemeMode,
    onSelected: (ThemeMode) -> Unit,
    onSources: () -> Unit,
    onAbout: () -> Unit,
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
            ListItem(
                headlineContent = { Text(stringResource(R.string.sources_and_rights)) },
                supportingContent = { Text(stringResource(R.string.sources_and_rights_summary)) },
                modifier = Modifier.fillMaxWidth().clickable(onClick = onSources),
            )
            ListItem(headlineContent = { Text(stringResource(R.string.about_rateel)) },
                supportingContent = { Text(BuildConfig.VERSION_NAME) },
                modifier = Modifier.fillMaxWidth().clickable(onClick = onAbout))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutRoute(onBack: () -> Unit, onSources: () -> Unit) {
    Scaffold(topBar = { TopAppBar(title = { Text(stringResource(R.string.about_rateel)) },
        navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack,
            contentDescription = stringResource(R.string.back)) } }) }) { padding ->
        Column(Modifier.padding(padding).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineMedium)
            Text("${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
            Text(stringResource(R.string.about_development))
            TextButton(onClick = onSources) { Text(stringResource(R.string.sources_and_rights)) }
            Text("github.com/moataz77549-byte/Tirlition")
        }
    }
}

@Composable
fun SourcesAndRightsRoute(
    onBack: () -> Unit,
    viewModel: SourcesViewModel = hiltViewModel(),
) {
    val sources by viewModel.sources.collectAsStateWithLifecycle()
    SourcesAndRightsScreen(sources = sources, onBack = onBack)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SourcesAndRightsScreen(
    sources: List<ContentSource>,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.sources_and_rights)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
            )
        },
    ) { padding ->
        if (sources.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(stringResource(R.string.sources_and_rights_empty))
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(sources, key = { it.id }) { source ->
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(source.name, style = MaterialTheme.typography.titleMedium)
                            Text(source.provider, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                text = if (source.isVerified) {
                                    stringResource(R.string.rights_verified)
                                } else {
                                    stringResource(R.string.rights_unverified)
                                },
                                style = MaterialTheme.typography.bodySmall,
                            )
                            source.attributionText?.takeIf { source.requiresAttribution }?.let {
                                Text(it, style = MaterialTheme.typography.bodySmall)
                            }
                            source.website?.let {
                                Text(it, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
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
