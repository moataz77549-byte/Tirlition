package com.rateel.app.ui.navigation

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
    val context = LocalContext.current
    val recordings by vm.recordings.collectAsStateWithLifecycle()
    val history by vm.history.collectAsStateWithLifecycle()
    val favorites by vm.favorites.collectAsStateWithLifecycle()
    val downloads by vm.downloads.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) }
    var rename by remember { mutableStateOf<LocalRecordingEntity?>(null) }
    var title by remember { mutableStateOf("") }
    var deleting by remember { mutableStateOf<LocalRecordingEntity?>(null) }
    var clearHistoryDialog by remember { mutableStateOf(false) }
    var clearFavoritesDialog by remember { mutableStateOf(false) }

    rename?.let { entry ->
        AlertDialog(
            onDismissRequest = { rename = null },
            title = { Text(stringResource(R.string.rename)) },
            text = { OutlinedTextField(title, { title = it }, label = { Text(stringResource(R.string.recording_name)) }) },
            confirmButton = {
                TextButton(onClick = { vm.rename(entry.id, title); rename = null }) {
                    Text(stringResource(R.string.save))
                }
            },
            dismissButton = { TextButton(onClick = { rename = null }) { Text(stringResource(R.string.cancel)) } }
        )
    }

    deleting?.let { entry ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text(stringResource(R.string.delete_recording)) },
            confirmButton = {
                TextButton(onClick = { vm.delete(entry.id); deleting = null }) {
                    Text(stringResource(R.string.delete_recording))
                }
            },
            dismissButton = { TextButton(onClick = { deleting = null }) { Text(stringResource(R.string.cancel)) } }
        )
    }

    if (clearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { clearHistoryDialog = false },
            title = { Text(stringResource(R.string.clear_history)) },
            text = { Text(stringResource(R.string.clear_history_confirm)) },
            confirmButton = {
                TextButton(onClick = { vm.clearHistory(); clearHistoryDialog = false }) {
                    Text(stringResource(R.string.clear_history))
                }
            },
            dismissButton = { TextButton(onClick = { clearHistoryDialog = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }

    if (clearFavoritesDialog) {
        AlertDialog(
            onDismissRequest = { clearFavoritesDialog = false },
            title = { Text(stringResource(R.string.clear_favorites)) },
            text = { Text(stringResource(R.string.clear_favorites_confirm)) },
            confirmButton = {
                TextButton(onClick = { vm.clearFavorites(); clearFavoritesDialog = false }) {
                    Text(stringResource(R.string.clear_favorites))
                }
            },
            dismissButton = { TextButton(onClick = { clearFavoritesDialog = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }

    Scaffold(topBar = { TopAppBar(title = { Text(stringResource(R.string.library)) }) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text(stringResource(R.string.downloaded_content)) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text(stringResource(R.string.recordings)) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text(stringResource(R.string.recent_listening)) }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = { Text(stringResource(R.string.favorites)) }
                )
            }

            when (selectedTab) {
                0 -> {
                    val completed = downloads.filter { it.status == "COMPLETED" }
                    if (completed.isEmpty()) {
                        Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text(stringResource(R.string.no_downloads), style = MaterialTheme.typography.bodyLarge)
                        }
                    } else {
                        LazyColumn(Modifier.fillMaxSize()) {
                            completed.groupBy { it.mushafId }.forEach { (mushaf, tracks) ->
                                item(key = "offline:${mushaf.orEmpty()}") {
                                    Text(
                                        "${mushaf.orEmpty()} · ${stringResource(R.string.downloaded_count, tracks.size)}",
                                        Modifier.padding(16.dp),
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                }
                                items(tracks, key = { "offline:${it.id}" }) { entry ->
                                    ListItem(
                                        headlineContent = { Text("${entry.surahNumber ?: ""} · ${entry.sourceId}") },
                                        supportingContent = { Text(readable(entry.fileSizeBytes)) },
                                        modifier = Modifier.fillMaxWidth().clickable { onSurah(entry.contentId) }
                                    )
                                }
                            }
                        }
                    }
                }
                1 -> {
                    if (recordings.isEmpty()) {
                        Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text(stringResource(R.string.no_recordings), style = MaterialTheme.typography.bodyLarge)
                        }
                    } else {
                        LazyColumn(Modifier.fillMaxSize(), contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(recordings, key = { it.id }) { entry ->
                                Card(Modifier.fillMaxWidth()) {
                                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(entry.title, style = MaterialTheme.typography.titleMedium)
                                        Text(
                                            "‎${entry.stationName} · ${DateFormat.getDateInstance().format(Date(entry.startedAt))} · ${(entry.durationMs ?: 0) / 60_000} min · ${(entry.fileSizeBytes ?: 0) / 1024} KB",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Button(onClick = { vm.play(entry.id) }) {
                                                Text(stringResource(R.string.play))
                                            }
                                            IconButton(onClick = {
                                                val shareIntent = vm.createShareIntent(entry.id)
                                                if (shareIntent != null) {
                                                    context.startActivity(Intent.createChooser(shareIntent, entry.title))
                                                }
                                            }) {
                                                Icon(Icons.Outlined.Share, contentDescription = stringResource(R.string.share))
                                            }
                                            TextButton(onClick = { title = entry.title; rename = entry }) {
                                                Text(stringResource(R.string.rename))
                                            }
                                            TextButton(
                                                onClick = { deleting = entry },
                                                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                            ) {
                                                Text(stringResource(R.string.delete_recording))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                2 -> {
                    if (history.isEmpty()) {
                        Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text(stringResource(R.string.no_history), style = MaterialTheme.typography.bodyLarge)
                        }
                    } else {
                        LazyColumn(Modifier.fillMaxSize()) {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "${stringResource(R.string.recent_listening)} (${history.size})",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    TextButton(
                                        onClick = { clearHistoryDialog = true },
                                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                    ) {
                                        Text(stringResource(R.string.clear_history))
                                    }
                                }
                            }
                            items(history, key = { "history:${it.id}" }) { entry ->
                                ListItem(
                                    headlineContent = { Text(entry.titleSnapshot.ifBlank { entry.contentId }) },
                                    supportingContent = {
                                        Text(DateFormat.getDateTimeInstance().format(Date(entry.playedAt)))
                                    }
                                )
                            }
                        }
                    }
                }
                3 -> {
                    if (favorites.isEmpty()) {
                        Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text(stringResource(R.string.no_favorites), style = MaterialTheme.typography.bodyLarge)
                        }
                    } else {
                        LazyColumn(Modifier.fillMaxSize()) {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "${stringResource(R.string.favorites)} (${favorites.size})",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    TextButton(
                                        onClick = { clearFavoritesDialog = true },
                                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                    ) {
                                        Text(stringResource(R.string.clear_favorites))
                                    }
                                }
                            }
                            items(favorites, key = { "favorite:${it.id}" }) { entry ->
                                ListItem(
                                    headlineContent = { Text(entry.contentId) },
                                    supportingContent = { Text(entry.contentType) },
                                    trailingContent = {
                                        IconButton(onClick = { vm.removeFavorite(entry.contentType, entry.contentId) }) {
                                            Icon(Icons.Outlined.Delete, contentDescription = stringResource(R.string.remove))
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
