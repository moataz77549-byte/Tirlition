package com.rateel.app.playback

import com.rateel.app.domain.model.PlaybackItem
import kotlinx.coroutines.flow.StateFlow

enum class PlaybackStatus { IDLE, PREPARING, BUFFERING, PLAYING, PAUSED, ENDED, ERROR, RECONNECTING }

data class UnifiedPlaybackState(
    val status: PlaybackStatus = PlaybackStatus.IDLE,
    val currentItem: PlaybackItem? = null,
    val positionMs: Long = 0,
    val durationMs: Long? = null,
    val bufferedPositionMs: Long = 0,
    val playWhenReady: Boolean = false,
    val error: String? = null,
    val queue: List<PlaybackItem> = emptyList(),
    val currentIndex: Int = -1,
)

interface PlaybackController {
    val state: StateFlow<UnifiedPlaybackState>
    fun play(item: PlaybackItem)
    fun playQueue(items: List<PlaybackItem>, startIndex: Int = 0)
    fun pause()
    fun resume()
    fun stop()
    fun seekTo(positionMs: Long)
    fun next()
    fun previous()
    fun setPlaybackSpeed(speed: Float)
    fun setRepeatMode(repeatMode: Int)
    fun setShuffle(enabled: Boolean)
    fun setSleepTimer(minutes: Int?)
}
