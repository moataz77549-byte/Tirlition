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
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rateel.app.R
import com.rateel.app.feature.player.PlayerViewModel
import com.rateel.app.playback.PlaybackStatus

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
    val item = state.currentItem
    Scaffold(topBar = { TopAppBar(title = { Text(stringResource(R.string.player)) },
        navigationIcon = { IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = stringResource(R.string.back))
        } }) }) { padding ->
        if (item != null) {
            Column(Modifier.fillMaxSize().padding(padding).padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)) {
                Text(item.title, style = MaterialTheme.typography.headlineMedium)
                item.subtitle?.let { Text(it, style = MaterialTheme.typography.titleMedium) }
                Text(if (item.isLive) stringResource(R.string.live) else state.status.name)
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
                state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        }
    }
}

private fun formatTime(ms: Long): String {
    val seconds = ms.coerceAtLeast(0) / 1000
    return "%02d:%02d".format(seconds / 60, seconds % 60)
}
