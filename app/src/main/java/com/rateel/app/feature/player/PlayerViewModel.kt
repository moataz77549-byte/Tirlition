package com.rateel.app.feature.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rateel.app.playback.PlaybackController
import com.rateel.app.playback.LastSessionResolver
import com.rateel.app.domain.repository.RadioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val controller: PlaybackController,
    private val restorer: LastSessionResolver,
    private val radios: RadioRepository,
) : ViewModel() {
    val state = controller.state
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
}
