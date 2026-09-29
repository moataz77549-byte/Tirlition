package com.rateel.app.ui.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rateel.app.R
import com.rateel.app.data.local.LocalRecordingEntity
import com.rateel.app.feature.library.LibraryViewModel
import java.text.DateFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryRoute(onSurah: (String) -> Unit = {}, vm: LibraryViewModel = hiltViewModel()) {
    val recordings by vm.recordings.collectAsStateWithLifecycle()
    val history by vm.history.collectAsStateWithLifecycle()
    val favorites by vm.favorites.collectAsStateWithLifecycle()
    val downloads by vm.downloads.collectAsStateWithLifecycle()
    var rename by remember { mutableStateOf<LocalRecordingEntity?>(null) }
    var title by remember { mutableStateOf("") }
    var deleting by remember { mutableStateOf<LocalRecordingEntity?>(null) }
    rename?.let { entry -> AlertDialog(onDismissRequest = { rename = null },
        title = { Text(stringResource(R.string.rename)) },
        text = { OutlinedTextField(title, { title = it }, label = { Text(stringResource(R.string.recording_name)) }) },
        confirmButton = { TextButton(onClick = { vm.rename(entry.id, title); rename = null }) {
            Text(stringResource(R.string.save)) } },
        dismissButton = { TextButton(onClick = { rename = null }) { Text(stringResource(R.string.cancel)) } }) }
    deleting?.let { entry -> AlertDialog(onDismissRequest = { deleting = null },
        title = { Text(stringResource(R.string.delete_recording)) },
        confirmButton = { TextButton(onClick = { vm.delete(entry.id); deleting = null }) {
            Text(stringResource(R.string.delete_recording)) } },
        dismissButton = { TextButton(onClick = { deleting = null }) { Text(stringResource(R.string.cancel)) } }) }
    Scaffold(topBar = { TopAppBar(title = { Text(stringResource(R.string.library)) }) }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding)) {
            item { Text(stringResource(R.string.downloaded_content), Modifier.padding(16.dp), style = MaterialTheme.typography.titleLarge) }
            val completed = downloads.filter { it.status == "COMPLETED" }
            if (completed.isEmpty()) item { Text(stringResource(R.string.no_downloads), Modifier.padding(16.dp)) }
            completed.groupBy { it.mushafId }.forEach { (mushaf, tracks) ->
                item(key = "offline:${mushaf.orEmpty()}") {
                    Text("${mushaf.orEmpty()} · ${stringResource(R.string.downloaded_count, tracks.size)}",
                        Modifier.padding(16.dp))
                }
                items(tracks, key = { "offline:${it.id}" }) { entry ->
                    ListItem(headlineContent = { Text("${entry.surahNumber ?: ""} · ${entry.sourceId}") },
                        supportingContent = { Text(readable(entry.fileSizeBytes)) },
                        modifier = Modifier.fillMaxWidth().clickable { onSurah(entry.contentId) })
                }
            }
            item { Text(stringResource(R.string.recordings), Modifier.padding(16.dp), style = MaterialTheme.typography.titleLarge) }
            if (recordings.isEmpty()) item { Text(stringResource(R.string.no_recordings), Modifier.padding(16.dp)) }
            items(recordings, key = { it.id }) { entry -> Column(Modifier.padding(8.dp)) {
                ListItem(headlineContent = { Text(entry.title) }, supportingContent = {
                    Text("‎${entry.stationName} · ${DateFormat.getDateInstance().format(Date(entry.startedAt))} · ${(entry.durationMs ?: 0) / 60_000} min · ${(entry.fileSizeBytes ?: 0) / 1024} KB") })
                Button(onClick = { vm.play(entry.id) }) { Text(stringResource(R.string.play)) }
                TextButton(onClick = { title = entry.title; rename = entry }) { Text(stringResource(R.string.rename)) }
                TextButton(onClick = { deleting = entry }) { Text(stringResource(R.string.delete_recording)) }
            } }
            item { Text(stringResource(R.string.recent_listening), Modifier.padding(16.dp), style = MaterialTheme.typography.titleLarge) }
            items(history, key = { "history:${it.id}" }) { entry ->
                ListItem(headlineContent = { Text(entry.titleSnapshot.ifBlank { entry.contentId }) },
                    supportingContent = { Text(DateFormat.getDateTimeInstance().format(Date(entry.playedAt))) }) }
            item { Text(stringResource(R.string.favorites), Modifier.padding(16.dp), style = MaterialTheme.typography.titleLarge) }
            items(favorites, key = { "favorite:${it.id}" }) { entry ->
                ListItem(headlineContent = { Text(entry.contentId) }, supportingContent = { Text(entry.contentType) }) }
        }
    }
}
