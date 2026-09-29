package com.rateel.app.ui.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
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
fun LibraryRoute(onSurah: (String) -> Unit = {}, onRadio: (String) -> Unit = {},
    vm: LibraryViewModel = hiltViewModel()) {
    val recordings by vm.recordings.collectAsStateWithLifecycle()
    val history by vm.history.collectAsStateWithLifecycle()
    val favorites by vm.favorites.collectAsStateWithLifecycle()
    val downloads by vm.downloads.collectAsStateWithLifecycle()
    val radios by vm.radios.collectAsStateWithLifecycle()
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
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = rateelListPadding,
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
            val completed = downloads.filter { it.status == "COMPLETED" }
            if (completed.isNotEmpty()) item { RateelSectionTitle(stringResource(R.string.downloaded_content)) }
            completed.groupBy { it.mushafId }.forEach { (mushaf, tracks) ->
                if (mushaf != null) item(key = "offline:$mushaf") {
                    RateelQuietCard(stringResource(R.string.audio_mushafs),
                        stringResource(R.string.downloaded_count, tracks.size)) }
                items(tracks, key = { "offline:${it.id}" }) { entry ->
                    ListItem(headlineContent = { Text(entry.surahNumber?.let {
                        stringResource(R.string.download_track_title, it) } ?: stringResource(R.string.audio_mushafs)) },
                        supportingContent = { Text(readable(entry.fileSizeBytes)) },
                        modifier = Modifier.fillMaxWidth().clickable { onSurah(entry.contentId) })
                }
            }
            if (recordings.isNotEmpty()) item { RateelSectionTitle(stringResource(R.string.recordings)) }
            items(recordings, key = { it.id }) { entry -> Card { Column(Modifier.padding(8.dp)) {
                ListItem(headlineContent = { Text(entry.title) }, supportingContent = {
                    Text("${entry.stationName} · ${DateFormat.getDateInstance().format(Date(entry.startedAt))} · ${readable(entry.fileSizeBytes)}") })
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(onClick = { vm.play(entry.id) }) { Text(stringResource(R.string.play)) }
                    TextButton(onClick = { title = entry.title; rename = entry }) { Text(stringResource(R.string.rename)) }
                    TextButton(onClick = { deleting = entry }) { Text(stringResource(R.string.delete_recording)) }
                }
            } } }
            if (history.isNotEmpty()) item { RateelSectionTitle(stringResource(R.string.recent_listening)) }
            items(history, key = { "history:${it.id}" }) { entry ->
                ListItem(headlineContent = { Text(entry.titleSnapshot.ifBlank { entry.contentId }) },
                    supportingContent = { Text(DateFormat.getDateTimeInstance().format(Date(entry.playedAt))) },
                    modifier = Modifier.clickable {
                        if (entry.contentType == "radio") onRadio(entry.contentId)
                        else if (entry.contentType == "surah") onSurah(entry.contentId)
                    }) }
            if (favorites.isNotEmpty()) item { RateelSectionTitle(stringResource(R.string.favorites)) }
            items(favorites, key = { "favorite:${it.id}" }) { entry ->
                ListItem(headlineContent = { Text(radios.firstOrNull { it.id == entry.contentId }?.nameArabic
                    ?: entry.contentId) }, modifier = Modifier.clickable {
                    if (entry.contentType == "radio") onRadio(entry.contentId)
                }) }
            if (completed.isEmpty() && recordings.isEmpty() && history.isEmpty() && favorites.isEmpty())
                item { RateelQuietCard(stringResource(R.string.library_empty), stringResource(R.string.quick_listen)) }
        }
    }
}
