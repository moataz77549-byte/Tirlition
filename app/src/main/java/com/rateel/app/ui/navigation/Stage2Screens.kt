package com.rateel.app.ui.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Radio
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rateel.app.R
import com.rateel.app.feature.radio.RadioViewModel
import com.rateel.app.feature.reciters.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RadioRoute(
    onPlayer: (String) -> Unit,
    viewModel: RadioViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    Scaffold(topBar = { TopAppBar(title = { Text(stringResource(R.string.radios)) }) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::setQuery,
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                singleLine = true,
                label = { Text(stringResource(R.string.search_radios)) },
            )
            if (state.offline) Text(stringResource(R.string.offline_metadata), Modifier.padding(horizontal = 16.dp))
            state.messageRes?.let { message ->
                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(stringResource(message), modifier = Modifier.weight(1f))
                    TextButton(onClick = viewModel::retry) { Text(stringResource(R.string.retry)) }
                }
            }
            if (state.loading) LinearProgressIndicator(Modifier.fillMaxWidth())
            LazyColumn(contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)) {
                items(state.items, key = { it.id }) { item ->
                    ListItem(
                        headlineContent = { Text(item.name) },
                        supportingContent = {
                            Column {
                                Text(listOfNotNull(item.category, item.sourceLabel).joinToString(" • "))
                                item.streamUrl?.let { Text(it.substringBefore("?"), maxLines = 1, style = MaterialTheme.typography.bodySmall) }
                                if (item.health != com.rateel.app.domain.model.StreamHealth.UNKNOWN) {
                                    Text(stringResource(R.string.stream_status, item.health.name), style = MaterialTheme.typography.labelSmall)
                                }
                                if (item.sourceHealth != com.rateel.app.domain.model.SourceHealth.UNKNOWN) {
                                    Text(stringResource(R.string.source_status, item.sourceHealth.name), style = MaterialTheme.typography.labelSmall)
                                }
                                item.attribution?.let { Text(it, style = MaterialTheme.typography.labelSmall) }
                            }
                        },
                        leadingContent = { Icon(Icons.Outlined.Radio, contentDescription = null) },
                        trailingContent = { Text(stringResource(R.string.live_badge), style = MaterialTheme.typography.labelMedium) },
                        modifier = Modifier.clickable(enabled = item.capabilities.canStream) { onPlayer(item.id) },
                    )
                    HorizontalDivider()
                }
                if (!state.loading && state.items.isEmpty()) item { Text(stringResource(R.string.no_radios), Modifier.padding(20.dp)) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecitersRoute(
    onReciter: (String) -> Unit,
    viewModel: RecitersViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    Scaffold(topBar = { TopAppBar(title = { Text(stringResource(R.string.reciters)) }) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::setQuery,
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                singleLine = true,
                label = { Text(stringResource(R.string.search_reciters)) },
            )
            if (state.offline) Text(stringResource(R.string.offline_metadata), Modifier.padding(horizontal = 16.dp))
            state.messageRes?.let { message ->
                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(stringResource(message), modifier = Modifier.weight(1f))
                    TextButton(onClick = viewModel::retry) { Text(stringResource(R.string.retry)) }
                }
            }
            if (state.loading) LinearProgressIndicator(Modifier.fillMaxWidth())
            LazyColumn(contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)) {
                items(state.items, key = { it.reciter.id }) { item ->
                    ListItem(
                        headlineContent = { Text(item.reciter.nameArabic) },
                        supportingContent = { Text(item.sourceLabel) },
                        modifier = Modifier.clickable { onReciter(item.reciter.id) },
                    )
                    HorizontalDivider()
                }
                if (!state.loading && state.items.isEmpty()) item { Text(stringResource(R.string.no_reciters), Modifier.padding(20.dp)) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReciterDetailRoute(
    onBack: () -> Unit,
    onMushaf: (String) -> Unit,
    viewModel: ReciterDetailViewModel = hiltViewModel(),
) {
    val items by viewModel.items.collectAsStateWithLifecycle()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.audio_mushafs)) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, stringResource(R.string.back)) } },
            )
        },
    ) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(12.dp)) {
            items(items, key = { it.mushaf.id }) { item ->
                ListItem(
                    headlineContent = { Text(item.mushaf.name) },
                    supportingContent = {
                        Column {
                            Text(if (item.isComplete) stringResource(R.string.complete_mushaf) else stringResource(R.string.available_surahs_count, item.availableCount))
                            Text(item.sourceLabel, style = MaterialTheme.typography.bodySmall)
                        }
                    },
                    modifier = Modifier.clickable { onMushaf(item.mushaf.id) },
                )
                HorizontalDivider()
            }
            if (items.isEmpty()) item { Text(stringResource(R.string.no_mushafs), Modifier.padding(20.dp)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MushafRoute(
    onBack: () -> Unit,
    onPlayer: (String) -> Unit,
    viewModel: MushafViewModel = hiltViewModel(),
) {
    val tracks by viewModel.tracks.collectAsStateWithLifecycle()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.surahs)) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, stringResource(R.string.back)) } },
            )
        },
    ) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(12.dp)) {
            items(tracks, key = { it.track.id }) { item ->
                ListItem(
                    headlineContent = { Text("${item.track.surahNumber}. ${item.track.surahNameArabic}") },
                    supportingContent = {
                        Text(
                            if (item.capabilities.canDownload) item.sourceLabel else stringResource(R.string.streaming_only_source, item.sourceLabel),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    },
                    modifier = Modifier.clickable(enabled = item.capabilities.canStream) { onPlayer(item.track.id) },
                )
                HorizontalDivider()
            }
            if (tracks.isEmpty()) item { Text(stringResource(R.string.no_surahs), Modifier.padding(20.dp)) }
        }
    }
}
