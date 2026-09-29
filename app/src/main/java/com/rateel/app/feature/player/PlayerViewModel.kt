package com.rateel.app.feature.player

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rateel.app.playback.PlaybackController
import com.rateel.app.playback.LastSessionResolver
import com.rateel.app.playback.RecordingService
import com.rateel.app.playback.StreamRecorder
import com.rateel.app.playback.RecordingStatus
import com.rateel.app.domain.repository.RadioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val controller: PlaybackController,
    private val restorer: LastSessionResolver,
    private val radios: RadioRepository,
    private val recorder: StreamRecorder,
    @ApplicationContext private val context: Context,
) : ViewModel() {
    val state = controller.state
    val recordingState = recorder.state
    fun toggle() {
        if (state.value.status == com.rateel.app.playback.PlaybackStatus.PLAYING) controller.pause()
        else controller.resume()
    }
    fun stop() = controller.stop()
    fun next() = controller.next()
    fun previous() = controller.previous()
    fun seekTo(positionMs: Long) = controller.seekTo(positionMs)
    fun setSleepTimer(minutes: Int?) = controller.setSleepTimer(minutes)
    fun retry() = viewModelScope.launch {
        val previous = state.value
        val item = previous.currentItem ?: return@launch
        if (item.isLive) radios.refresh()
        val fresh = restorer.resolve(item.id) ?: return@launch
        val queue = previous.queue.toMutableList()
        if (previous.currentIndex in queue.indices) {
            queue[previous.currentIndex] = fresh
            controller.playQueue(queue, previous.currentIndex)
        } else controller.play(fresh)
    }
    fun record(minutes: Int?) {
        val item = state.value.currentItem ?: return
        if (!item.isLive || !item.capabilities.canRecord ||
            (minutes != null && minutes !in listOf(5, 10, 15, 30)) ||
            recordingState.value.status in setOf(RecordingStatus.PREPARING,
                RecordingStatus.RECORDING, RecordingStatus.FINALIZING)) return
        val url = item.remoteUri ?: return
        ContextCompat.startForegroundService(context, Intent(context, RecordingService::class.java).apply {
            action = RecordingService.ACTION_START
            putExtra(RecordingService.EXTRA_STATION, item.stationId ?: item.id)
            putExtra(RecordingService.EXTRA_NAME, item.title)
            putExtra(RecordingService.EXTRA_SOURCE, item.sourceId)
            putExtra(RecordingService.EXTRA_URL, url)
            putExtra(RecordingService.EXTRA_FORMAT, when (item.mimeType) {
                "audio/mpeg" -> "mp3"; "audio/aac" -> "aac"; "application/x-mpegURL" -> "hls"; else -> null
            })
            putExtra(RecordingService.EXTRA_MINUTES, minutes ?: 0)
        })
    }
    fun stopRecording(cancel: Boolean = false) {
        context.startService(Intent(context, RecordingService::class.java).apply {
            action = if (cancel) RecordingService.ACTION_CANCEL else RecordingService.ACTION_STOP
        })
    }
}
