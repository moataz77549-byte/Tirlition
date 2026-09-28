package com.rateel.app.ui.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rateel.app.R
import com.rateel.app.domain.model.Mushaf
import com.rateel.app.domain.model.SurahAudio
import com.rateel.app.feature.catalog.CatalogViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RadiosRoute(onStation: (String) -> Unit, vm: CatalogViewModel = hiltViewModel()) {
    val radios by vm.radioRows.collectAsStateWithLifecycle()
    var search by remember { mutableStateOf("") }
    LaunchedEffect(Unit) { vm.refreshCatalog() }
    Scaffold(topBar = {
        TopAppBar(title = { Text(stringResource(R.string.radios)) },
            actions = { IconButton(onClick = { vm.refreshCatalog() }) {
                Icon(Icons.Outlined.Refresh, contentDescription = stringResource(R.string.retry))
            } })
    }) { padding ->
        Column(Modifier.padding(padding)) {
            OutlinedTextField(value = search, onValueChange = { search = it },
                label = { Text(stringResource(R.string.search_radios)) },
                modifier = Modifier.fillMaxWidth().padding(12.dp), singleLine = true)
            LazyColumn(contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val filtered = radios.filter {
                    search.isBlank() || it.station.nameArabic.contains(search, ignoreCase = true) ||
                        it.station.sourceId.contains(search, ignoreCase = true) ||
                        it.station.category.orEmpty().contains(search, ignoreCase = true)
                }
                items(filtered, key = { it.station.id }) { row ->
                    Card(Modifier.fillMaxWidth().clickable { onStation(row.station.id) }) {
                        ListItem(headlineContent = { Text(row.station.nameArabic) },
                            supportingContent = {
                                Text("${row.station.sourceId} · ${row.station.health.name}")
                            }, trailingContent = { Text(stringResource(R.string.live)) })
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RadioDetailRoute(id: String, onBack: () -> Unit, vm: CatalogViewModel = hiltViewModel()) {
    val radios by vm.radioRows.collectAsStateWithLifecycle()
    val row = radios.firstOrNull { it.station.id == id }
    LaunchedEffect(id) { vm.refreshCatalog() }
    Scaffold(topBar = { TopAppBar(title = { Text(stringResource(R.string.radios)) },
        navigationIcon = { IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = stringResource(R.string.back))
        } }) }) { padding ->
        Column(Modifier.padding(padding).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(row?.station?.nameArabic.orEmpty(), style = MaterialTheme.typography.headlineMedium)
            row?.let {
                Text(stringResource(R.string.live))
                Text("${stringResource(R.string.sources_and_rights)}: ${it.station.sourceId}")
                Text("${stringResource(R.string.stream_address)}: ${it.station.streams.firstOrNull()?.url.orEmpty()}")
                if (!it.capabilities.canRecord) Text(stringResource(R.string.recording_unavailable))
                Text(stringResource(R.string.player_next_stage))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurahDetailRoute(id: String, onBack: () -> Unit, vm: CatalogViewModel = hiltViewModel()) {
    val mushafId = id.substringBeforeLast(':', "")
    val flow = remember(mushafId) { vm.tracks(mushafId) }
    val tracks by flow.collectAsStateWithLifecycle(initialValue = emptyList())
    val track = tracks.firstOrNull { it.id == id }
    Scaffold(topBar = { TopAppBar(title = { Text(stringResource(R.string.surahs)) },
        navigationIcon = { IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = stringResource(R.string.back))
        } }) }) { padding ->
        Column(Modifier.padding(padding).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(track?.surahNameArabic.orEmpty(), style = MaterialTheme.typography.headlineMedium)
            track?.let {
                Text("${stringResource(R.string.sources_and_rights)}: ${it.sourceId}")
                Text(stringResource(R.string.player_next_stage))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecitersRoute(onReciter: (String) -> Unit, vm: CatalogViewModel = hiltViewModel()) {
    val reciters by vm.reciterRows.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { vm.refreshCatalog() }
    Scaffold(topBar = { TopAppBar(title = { Text(stringResource(R.string.reciters)) }) }) { padding ->
        LazyColumn(Modifier.padding(padding), contentPadding = PaddingValues(12.dp)) {
            items(reciters, key = { it.id }) { reciter ->
                ListItem(headlineContent = { Text(reciter.nameArabic) },
                    supportingContent = { Text(reciter.sourceId) },
                    modifier = Modifier.fillMaxWidth().clickable { onReciter(reciter.id) })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MushafsRoute(id: String, onBack: () -> Unit, onMushaf: (String) -> Unit,
                 vm: CatalogViewModel = hiltViewModel()) {
    val flow = remember(id) { vm.mushafs(id) }
    val rows by flow.collectAsStateWithLifecycle(initialValue = emptyList())
    LaunchedEffect(id) { vm.refreshMushafs(id) }
    CatalogListScaffold(stringResource(R.string.audio_mushafs), onBack) {
        items(rows, key = { it.id }) { mushaf: Mushaf ->
            ListItem(headlineContent = { Text(mushaf.name) },
                supportingContent = { Text("${mushaf.riwaya} · ${stringResource(R.string.available_surahs, mushaf.availableSurahs.size)} · ${mushaf.sourceId}") },
                modifier = Modifier.fillMaxWidth().clickable { onMushaf(mushaf.id) })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurahsRoute(id: String, onBack: () -> Unit, onSurah: (String) -> Unit,
                vm: CatalogViewModel = hiltViewModel()) {
    val flow = remember(id) { vm.tracks(id) }
    val rows by flow.collectAsStateWithLifecycle(initialValue = emptyList())
    LaunchedEffect(id) { vm.refreshTracks(id) }
    CatalogListScaffold(stringResource(R.string.surahs), onBack) {
        items(rows, key = { it.id }) { track: SurahAudio ->
            ListItem(headlineContent = { Text("${track.surahNumber}. ${track.surahNameArabic}") },
                supportingContent = { Text(track.sourceId) },
                modifier = Modifier.fillMaxWidth().clickable { onSurah(track.id) })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CatalogListScaffold(title: String, onBack: () -> Unit,
    content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit) {
    Scaffold(topBar = { TopAppBar(title = { Text(title) }, navigationIcon = {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = stringResource(R.string.back))
        }
    }) }) { padding ->
        LazyColumn(Modifier.padding(padding), contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp), content = content)
    }
}
