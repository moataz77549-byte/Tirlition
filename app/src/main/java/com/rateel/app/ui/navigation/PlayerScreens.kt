package com.rateel.app.ui.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material.icons.outlined.SkipPrevious
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material.icons.outlined.Radio
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rateel.app.R
import com.rateel.app.feature.player.PlayerViewModel
import com.rateel.app.playback.PlaybackStatus
import com.rateel.app.playback.RecordingStatus
import coil3.compose.AsyncImage

@Composable
fun MiniPlayer(onOpen: () -> Unit, vm: PlayerViewModel) {
    val state by vm.state.collectAsStateWithLifecycle()
    val item = state.currentItem ?: return
    Column {
        if (!item.isLive && state.durationMs != null) {
            LinearProgressIndicator(
                progress = { (state.positionMs.toFloat() / state.durationMs!!.coerceAtLeast(1)).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
        ListItem(
            headlineContent = { Text(item.title, maxLines = 1) },
            supportingContent = { Text(if (item.isLive) stringResource(R.string.live) else item.subtitle.orEmpty()) },
            leadingContent = {
                if (item.artwork != null) AsyncImage(model = item.artwork, contentDescription = null,
                    modifier = Modifier.size(48.dp))
                else Icon(if (item.isLive) Icons.Outlined.Radio else Icons.Outlined.LibraryMusic,
                    contentDescription = null, modifier = Modifier.size(40.dp))
            },
            modifier = Modifier.fillMaxWidth().clickable(onClick = onOpen),
            trailingContent = {
                IconButton(onClick = vm::toggle) {
                    Icon(if (state.status == PlaybackStatus.PLAYING) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                        contentDescription = stringResource(if (state.status == PlaybackStatus.PLAYING) R.string.pause else R.string.play))
                }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullPlayerRoute(onBack: () -> Unit, vm: PlayerViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val recording by vm.recordingState.collectAsStateWithLifecycle()
    val item = state.currentItem
    var timerDialog by remember { mutableStateOf(false) }
    var recordingDialog by remember { mutableStateOf(false) }
    if (recordingDialog) AlertDialog(onDismissRequest = { recordingDialog = false },
        title = { Text(stringResource(R.string.save_clip)) },
        text = { Column {
            Text(stringResource(R.string.recording_description))
            listOf(5, 10, 15, 30).forEach { minutes ->
                TextButton(onClick = { vm.record(minutes); recordingDialog = false }) {
                    Text(stringResource(R.string.minutes_option, minutes))
                }
            }
            TextButton(onClick = { vm.record(null); recordingDialog = false }) {
                Text(stringResource(R.string.free_recording))
            }
        } }, confirmButton = {})
    if (timerDialog) AlertDialog(
        onDismissRequest = { timerDialog = false },
        title = { Text(stringResource(R.string.sleep_timer)) },
        text = { Column {
            listOf(5, 10, 15, 30, 45, 60).forEach { minutes ->
                TextButton(onClick = { vm.setSleepTimer(minutes); timerDialog = false }) {
                    Text(stringResource(R.string.minutes_option, minutes))
                }
            }
            if (item?.isLive == false) TextButton(onClick = { vm.setSleepTimer(-1); timerDialog = false }) {
                Text(stringResource(R.string.end_of_surah))
            }
            TextButton(onClick = { vm.setSleepTimer(null); timerDialog = false }) {
                Text(stringResource(R.string.cancel_timer))
            }
        } },
        confirmButton = {},
    )
    Scaffold(topBar = { TopAppBar(title = { Text(stringResource(R.string.player)) },
        navigationIcon = { IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = stringResource(R.string.back))
        } }) }) { padding ->
        if (item != null) {
            Column(Modifier.fillMaxSize().padding(padding).padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)) {
                item.artwork?.let { artwork ->
                    AsyncImage(model = artwork, contentDescription = null, modifier = Modifier.size(200.dp))
                } ?: Icon(if (item.isLive) Icons.Outlined.Radio else Icons.Outlined.LibraryMusic,
                    contentDescription = null, modifier = Modifier.size(160.dp))
                }
                Text(item.title, style = MaterialTheme.typography.headlineMedium)
                item.subtitle?.let { Text(it, style = MaterialTheme.typography.titleMedium) }
                if (item.isLive) Text(stringResource(R.string.live))
                if (state.status == PlaybackStatus.BUFFERING ||
                    state.status == PlaybackStatus.PREPARING ||
                    state.status == PlaybackStatus.RECONNECTING) {
                    CircularProgressIndicator()
                    Text(stringResource(if (state.status == PlaybackStatus.RECONNECTING)
                        R.string.reconnecting else R.string.buffering))
                }
                if (!item.isLive && state.durationMs != null) {
                    Slider(
                        value = state.positionMs.toFloat().coerceIn(0f, state.durationMs!!.toFloat()),
                        onValueChange = { vm.seekTo(it.toLong()) },
                        valueRange = 0f..state.durationMs!!.toFloat().coerceAtLeast(1f),
                    )
                    Text("${formatTime(state.positionMs)} / ${formatTime(state.durationMs!!)}")
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    IconButton(onClick = vm::previous, enabled = state.currentIndex > 0) {
                        Icon(Icons.Outlined.SkipPrevious, contentDescription = stringResource(R.string.back))
                    }
                    IconButton(onClick = vm::toggle) {
                        Icon(if (state.status == PlaybackStatus.PLAYING) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                            contentDescription = stringResource(if (state.status == PlaybackStatus.PLAYING) R.string.pause else R.string.play))
                    }
                    IconButton(onClick = vm::next, enabled = state.currentIndex < state.queue.lastIndex) {
                        Icon(Icons.Outlined.SkipNext, contentDescription = stringResource(R.string.player))
                    }
                    IconButton(onClick = vm::stop) {
                        Icon(Icons.Outlined.Stop, contentDescription = stringResource(R.string.stop))
                    }
                }
                Text("${stringResource(R.string.sources_and_rights)}: ${item.sourceId}")
                TextButton(onClick = { timerDialog = true }) { Text(stringResource(R.string.sleep_timer)) }
                if (item.isLive && item.capabilities.canRecord &&
                    recording.status !in setOf(RecordingStatus.PREPARING,
                        RecordingStatus.RECORDING, RecordingStatus.FINALIZING)) {
                    Button(onClick = { recordingDialog = true }) { Text(stringResource(R.string.save_clip)) }
                }
                if (recording.status == RecordingStatus.RECORDING) {
                    Text(stringResource(R.string.recording_active) + " · " + (recording.elapsedMs / 1000) + "s")
                    TextButton(onClick = { vm.stopRecording() }) { Text(stringResource(R.string.stop_recording)) }
                    TextButton(onClick = { vm.stopRecording(cancel = true) }) {
                        Text(stringResource(R.string.cancel_recording))
                    }
                }
                state.error?.let { error ->
                    val message = when {
                        "NETWORK" in error -> R.string.error_network
                        "HTTP" in error -> R.string.stream_unavailable
                        "DECODING" in error || "PARSING" in error -> R.string.unsupported_audio
                        else -> R.string.stream_unavailable
                    }
                    Text(stringResource(message), color = MaterialTheme.colorScheme.error)
                    TextButton(onClick = vm::retry) { Text(stringResource(R.string.retry)) }
                }
            }
        }
    }
}

private fun formatTime(ms: Long): String {
    val seconds = ms.coerceAtLeast(0) / 1000
    return "%02d:%02d".format(seconds / 60, seconds % 60)
}
