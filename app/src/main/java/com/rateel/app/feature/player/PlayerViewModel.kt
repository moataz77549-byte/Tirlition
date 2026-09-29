package com.rateel.app.feature.player

import androidx.lifecycle.ViewModel
import com.rateel.app.playback.PlaybackController
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(private val controller: PlaybackController) : ViewModel() {
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
}
