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
import com.rateel.app.core.model.AppResult
import com.rateel.app.domain.model.Mushaf
import com.rateel.app.domain.model.SurahAudio
import com.rateel.app.domain.model.StreamHealth
import com.rateel.app.feature.catalog.CatalogViewModel
import com.rateel.app.domain.source.ArabicSearch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RadiosRoute(onStation: (String) -> Unit, vm: CatalogViewModel = hiltViewModel()) {
    val radios by vm.radioRows.collectAsStateWithLifecycle()
    val error by vm.error.collectAsStateWithLifecycle()
    val refreshing by vm.refreshing.collectAsStateWithLifecycle()
    var search by remember { mutableStateOf("") }
    var category by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) { vm.refreshCatalog() }
    Scaffold(topBar = {
        TopAppBar(title = { Text(stringResource(R.string.radios)) },
            actions = { IconButton(onClick = { vm.refreshCatalog(force = true) }) {
                Icon(Icons.Outlined.Refresh, contentDescription = stringResource(R.string.retry))
            } })
    }) { padding ->
        Column(Modifier.padding(padding)) {
            if (refreshing) LinearProgressIndicator(Modifier.fillMaxWidth())
            error?.let { RateelQuietCard(stringResource(it.messageRes()),
                stringResource(R.string.offline_cached_content), Modifier.padding(horizontal = 16.dp)) }
            Box(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                RateelSearch(search, { search = it }, stringResource(R.string.search_radios))
            }
            val categories = radios.mapNotNull { it.station.category?.takeIf(String::isNotBlank) }.distinct().take(8)
            if (categories.isNotEmpty()) {
                androidx.compose.foundation.lazy.LazyRow(contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item { FilterChip(selected = category == null, onClick = { category = null },
                        label = { Text(stringResource(R.string.all)) }) }
                    items(categories) { name -> FilterChip(selected = category == name,
                        onClick = { category = name }, label = { Text(name) }) }
                }
            }
            LazyColumn(contentPadding = rateelListPadding, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val filtered = radios.filter {
                    (category == null || category == it.station.category) &&
                        (search.isBlank() || ArabicSearch.matches(it.station.nameArabic, search) ||
                            it.station.category.orEmpty().contains(search, ignoreCase = true))
                }
                if (filtered.isEmpty() && !refreshing) item {
                    RateelQuietCard(stringResource(R.string.no_search_results), stringResource(R.string.retry)) }
                items(filtered, key = { it.station.id }) { row ->
                    Card(Modifier.fillMaxWidth().clickable { onStation(row.station.id) }) {
                        ListItem(headlineContent = { Text(row.station.nameArabic) },
                            supportingContent = { row.station.category?.let { Text(it) } },
                            trailingContent = { Text(stringResource(if (row.station.health == StreamHealth.OFFLINE ||
                                row.station.health == StreamHealth.BLOCKED) R.string.radio_status_unavailable
                                else R.string.radio_live_label), style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary) })
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
    val validation by vm.streamValidation.collectAsStateWithLifecycle()
    val row = radios.firstOrNull { it.station.id == id }
    val recording by vm.recordingState.collectAsStateWithLifecycle()
    val favoriteIds by vm.favoriteIds.collectAsStateWithLifecycle()
    var recordingDialog by remember { mutableStateOf(false) }
    if (recordingDialog) AlertDialog(onDismissRequest = { recordingDialog = false },
        title = { Text(stringResource(R.string.save_clip)) },
        text = { Column {
            Text(stringResource(R.string.recording_description))
            listOf(5, 10, 15, 30).forEach { minutes ->
                TextButton(onClick = { vm.recordRadio(id, minutes); recordingDialog = false }) {
                    Text(stringResource(R.string.minutes_option, minutes)) }
            }
            TextButton(onClick = { vm.recordRadio(id, null); recordingDialog = false }) {
                Text(stringResource(R.string.free_recording)) }
        } }, confirmButton = {})
    LaunchedEffect(id) { vm.refreshCatalog() }
    val endpointUrl = row?.station?.streams?.firstOrNull()?.url
    LaunchedEffect(endpointUrl) { endpointUrl?.let(vm::validateStream) }
    Scaffold(topBar = { TopAppBar(title = { Text(stringResource(R.string.radios)) },
        navigationIcon = { IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = stringResource(R.string.back))
        } }) }) { padding ->
        Column(Modifier.padding(padding).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(row?.station?.nameArabic.orEmpty(), style = MaterialTheme.typography.headlineMedium)
            row?.let {
                Text(stringResource(R.string.live))
                if ((validation?.health ?: it.station.health) == StreamHealth.OFFLINE)
                    Text(stringResource(R.string.radio_status_unavailable))
                if (it.capabilities.requiresAttribution)
                    Text(stringResource(R.string.source_info, sourceDisplayName(it.station.sourceId)),
                        style = MaterialTheme.typography.labelSmall)
                Button(onClick = { vm.playRadio(id) }) { Text(stringResource(R.string.play)) }
                TextButton(onClick = { vm.toggleRadioFavorite(id) }) {
                    Text(stringResource(if (id in favoriteIds) R.string.remove_favorite else R.string.add_favorite))
                }
                if (it.capabilities.canRecord) {
                    Button(onClick = { recordingDialog = true },
                        enabled = recording.status !in setOf(com.rateel.app.playback.RecordingStatus.PREPARING,
                            com.rateel.app.playback.RecordingStatus.RECORDING,
                            com.rateel.app.playback.RecordingStatus.FINALIZING)) {
                        Text(stringResource(R.string.save_clip))
                    }
                }
                if (recording.status == com.rateel.app.playback.RecordingStatus.RECORDING) {
                    Text(stringResource(R.string.recording_active) + " · " + (recording.elapsedMs / 1000) + "s")
                    TextButton(onClick = { vm.stopRecording() }) { Text(stringResource(R.string.stop_recording)) }
                    TextButton(onClick = { vm.stopRecording(cancel = true) }) { Text(stringResource(R.string.cancel_recording)) }
                }
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
    val downloads by vm.downloadRows.collectAsStateWithLifecycle(initialValue = emptyList())
    val eligible by vm.downloadableSources.collectAsStateWithLifecycle(initialValue = emptySet())
    Scaffold(topBar = { TopAppBar(title = { Text(stringResource(R.string.surahs)) },
        navigationIcon = { IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = stringResource(R.string.back))
        } }) }) { padding ->
        Column(Modifier.padding(padding).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(track?.surahNameArabic.orEmpty(), style = MaterialTheme.typography.headlineMedium)
            track?.let {
                // An asset may require visible attribution even when the technical ID is hidden.
                Button(onClick = { vm.playSurah(id) }) { Text(stringResource(R.string.play)) }
                if (it.sourceId in eligible) {
                    val row = downloads.firstOrNull { entry -> entry.contentId == id }
                    if (row == null || row.status !in setOf("QUEUED", "DOWNLOADING", "VERIFYING", "COMPLETED"))
                        TextButton(onClick = { vm.downloadSurah(it) }) { Text(stringResource(R.string.download_surah)) }
                    else Text(row.status)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecitersRoute(onReciter: (String) -> Unit, vm: CatalogViewModel = hiltViewModel()) {
    val reciters by vm.reciterRows.collectAsStateWithLifecycle()
    val error by vm.error.collectAsStateWithLifecycle()
    val refreshing by vm.refreshing.collectAsStateWithLifecycle()
    var search by remember { mutableStateOf("") }
    LaunchedEffect(Unit) { vm.refreshCatalog() }
    Scaffold(topBar = { TopAppBar(title = { Text(stringResource(R.string.reciters)) }) }) { padding ->
        LazyColumn(Modifier.padding(padding), contentPadding = rateelListPadding,
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item { RateelSearch(search, { search = it }, stringResource(R.string.search_reciters)) }
            if (refreshing) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
            error?.let { item { Text(stringResource(it.messageRes())) } }
            val filtered = reciters.filter { ArabicSearch.matches(it.nameArabic, search) }
            if (filtered.isEmpty() && !refreshing) item { Text(stringResource(R.string.no_search_results)) }
            items(filtered, key = { it.id }) { reciter ->
                Card(Modifier.fillMaxWidth().clickable { onReciter(reciter.id) }) {
                    ListItem(headlineContent = { Text(reciter.nameArabic) },
                        supportingContent = { reciter.country?.let { Text(it) } })
                }
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
    val error by vm.error.collectAsStateWithLifecycle()
    LaunchedEffect(id) { vm.refreshMushafs(id) }
    CatalogListScaffold(stringResource(R.string.audio_mushafs), onBack) {
        error?.let { item { Text(stringResource(it.messageRes())) } }
        if (rows.isEmpty()) item { Text(stringResource(R.string.home_empty)) }
        items(rows, key = { it.id }) { mushaf: Mushaf ->
            ListItem(headlineContent = { Text(mushaf.name) },
                supportingContent = { Text("${mushaf.riwaya} · ${stringResource(R.string.available_surahs, mushaf.availableSurahs.size)}") },
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
    val error by vm.error.collectAsStateWithLifecycle()
    val downloads by vm.downloadRows.collectAsStateWithLifecycle(initialValue = emptyList())
    val eligible by vm.downloadableSources.collectAsStateWithLifecycle(initialValue = emptySet())
    var freeBytes by remember { mutableLongStateOf(0L) }
    LaunchedEffect(Unit) { freeBytes = vm.freeStorageBytes() }
    var selected by remember(id) { mutableStateOf(setOf<String>()) }
    var confirm by remember { mutableStateOf(false) }
    if (confirm) AlertDialog(onDismissRequest = { confirm = false },
        title = { Text(stringResource(R.string.download_mushaf)) },
        text = { Text(stringResource(R.string.download_confirm, rows.size,
            readable(rows.mapNotNull { it.fileSizeBytes }.takeIf { it.size == rows.size }?.sum()), readable(freeBytes))) },
        confirmButton = { TextButton(onClick = { vm.downloadTracks(rows); confirm = false }) { Text(stringResource(R.string.download_mushaf)) } },
        dismissButton = { TextButton(onClick = { confirm = false }) { Text(stringResource(R.string.cancel)) } })
    LaunchedEffect(id) { vm.refreshTracks(id) }
    CatalogListScaffold(stringResource(R.string.surahs), onBack) {
        error?.let { item { Text(stringResource(it.messageRes())) } }
        if (rows.isEmpty()) item { Text(stringResource(R.string.home_empty)) }
        if (rows.isNotEmpty() && rows.all { it.sourceId in eligible }) {
            item { Button(onClick = { confirm = true }) { Text(stringResource(R.string.download_mushaf)) } }
            item { if (selected.isNotEmpty()) Button(onClick = { vm.downloadTracks(rows.filter { it.id in selected }); selected = emptySet() }) {
                Text(stringResource(R.string.download_selected, selected.size)) } }
        }
        items(rows, key = { it.id }) { track: SurahAudio ->
            ListItem(headlineContent = { Text("${track.surahNumber}. ${track.surahNameArabic}") },
                supportingContent = { downloads.firstOrNull { it.contentId == track.id }?.let { row ->
                    Text(stringResource(when (row.status) {
                        "COMPLETED" -> R.string.download_complete
                        "DOWNLOADING" -> R.string.download_active
                        "PAUSED" -> R.string.download_paused
                        "WAITING_FOR_NETWORK" -> R.string.download_waiting
                        "FAILED" -> R.string.download_failed
                        else -> R.string.download_queued
                    })) } },
                trailingContent = { if (track.sourceId in eligible)
                    Checkbox(checked = track.id in selected, onCheckedChange = { checked -> selected = if (checked) selected + track.id else selected - track.id }) },
                modifier = Modifier.fillMaxWidth().clickable { onSurah(track.id) })
        }
    }
}

private fun AppResult.Error.messageRes(): Int = when (this) {
    AppResult.Error.Network -> R.string.error_network
    AppResult.Error.Server -> R.string.error_server
    AppResult.Error.Timeout -> R.string.error_timeout
    AppResult.Error.Parsing -> R.string.error_parsing
    is AppResult.Error.Unknown -> R.string.error_unknown
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
