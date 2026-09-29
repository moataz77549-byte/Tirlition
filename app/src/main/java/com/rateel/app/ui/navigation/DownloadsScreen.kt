package com.rateel.app.ui.navigation

import androidx.compose.foundation.layout.*
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
import com.rateel.app.data.local.DownloadEntity
import com.rateel.app.feature.download.DownloadsViewModel

fun readable(bytes: Long?): String = bytes?.let { "%.1f MB".format(it / 1048576.0) } ?: "—"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadsRoute(vm: DownloadsViewModel = hiltViewModel()) {
    val rows by vm.downloads.collectAsStateWithLifecycle()
    val wifi by vm.wifiOnly.collectAsStateWithLifecycle()
    val usage by vm.storage.collectAsStateWithLifecycle()
    val error by vm.error.collectAsStateWithLifecycle()
    LaunchedEffect(rows) { vm.refreshStorage() }
    Scaffold(topBar = { TopAppBar(title = { Text(stringResource(R.string.downloads)) }) }) { padding ->
        LazyColumn(Modifier.padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item { Row { Text(stringResource(R.string.download_wifi_only), Modifier.weight(1f)); Switch(wifi, vm::wifiOnly) } }
            item { Text(stringResource(R.string.storage_usage, readable(usage.first), readable(usage.second), readable(usage.third))) }
            error?.let { item { Text(it, color = MaterialTheme.colorScheme.error) } }
            if (rows.isEmpty()) item { Text(stringResource(R.string.home_empty)) }
            val groups = rows.filter { it.mushafId != null }.groupBy { it.mushafId!! }
            groups.forEach { (mushaf, group) ->
                item(key = "group:$mushaf") {
                    Text(mushaf, style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(R.string.download_partial, group.count { it.status == "COMPLETED" }, group.size))
                    TextButton(onClick = { vm.deleteMushaf(mushaf) }) { Text(stringResource(R.string.download_delete)) }
                }
                items(group, key = { it.id }) { DownloadCard(it, vm) }
            }
            items(rows.filter { it.mushafId == null }, key = { it.id }) { DownloadCard(it, vm) }
        }
    }
}

@Composable
private fun DownloadCard(row: DownloadEntity, vm: DownloadsViewModel) {
    val percent = row.totalBytes?.takeIf { it > 0 }?.let { (row.bytesDownloaded * 100 / it).toInt().coerceIn(0, 100) }
    Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("${row.surahNumber ?: ""} · ${row.sourceId}")
        Text(if (row.status == "VERIFYING") stringResource(R.string.download_verifying) else row.status)
        if (percent != null) LinearProgressIndicator(progress = { percent / 100f }, modifier = Modifier.fillMaxWidth())
        Text("${percent?.let { "$it% · " }.orEmpty()}${readable(row.bytesDownloaded)} / ${readable(row.totalBytes)}")
        Row {
            when (row.status) {
                "DOWNLOADING", "QUEUED", "WAITING_FOR_NETWORK" -> TextButton(onClick = { vm.pause(row) }) { Text(stringResource(R.string.download_pause)) }
                "PAUSED", "FAILED", "MISSING_FILE" -> TextButton(onClick = { vm.resume(row) }) { Text(stringResource(R.string.download_retry)) }
            }
            if (row.status !in setOf("COMPLETED", "CANCELLED")) TextButton(onClick = { vm.cancel(row) }) { Text(stringResource(R.string.download_cancel)) }
            if (row.status == "COMPLETED") TextButton(onClick = { vm.delete(row) }) { Text(stringResource(R.string.download_delete)) }
        }
    } }
}
