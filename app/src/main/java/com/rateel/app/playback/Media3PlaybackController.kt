package com.rateel.app.playback

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.rateel.app.domain.model.PlaybackItem
import com.rateel.app.data.local.PlaybackProgressDao
import com.rateel.app.data.settings.AppSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Singleton
class Media3PlaybackController @Inject constructor(
    @ApplicationContext context: Context,
    private val progressDao: PlaybackProgressDao,
    private val settings: AppSettings,
    private val restorer: LastSessionResolver,
) : PlaybackController {
    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val stateMutable = MutableStateFlow(UnifiedPlaybackState())
    override val state: StateFlow<UnifiedPlaybackState> = stateMutable
    private var controller: MediaController? = null
    private var pending: ((MediaController) -> Unit)? = null
    private var items = emptyList<PlaybackItem>()
    private var restored = false
    private val future = MediaController.Builder(
        context,
        SessionToken(context, ComponentName(context, UnifiedPlaybackService::class.java)),
    ).buildAsync()

    init {
        future.addListener({
            runCatching { future.get() }.onSuccess { connected ->
                controller = connected
                connected.addListener(object : Player.Listener {
                    override fun onEvents(player: Player, events: Player.Events) = update(player)
                    override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                        stateMutable.value = stateMutable.value.copy(
                            status = PlaybackStatus.ERROR, error = error.errorCodeName,
                        )
                    }
                })
                update(connected)
                pending?.invoke(connected)
                pending = null
                if (connected.mediaItemCount == 0) scope.launch {
                    val preferences = settings.preferences.first()
                    val id = preferences.lastPlaybackItemId
                    if (preferences.autoResume && id != null && connected.mediaItemCount == 0) {
                        val item = restorer.resolve(id)
                        if (item != null && connected.mediaItemCount == 0) {
                            restored = true
                            items = listOf(item)
                            connected.setMediaItem(PlaybackMediaItemMapper.toMediaItem(item))
                            if (!item.isLive) {
                                val saved = kotlinx.coroutines.withContext(Dispatchers.IO) { progressDao.get(id) }
                                if (saved != null && !saved.completed) connected.seekTo(saved.positionMs)
                            }
                            update(connected)
                        }
                    }
                }
            }.onFailure {
                stateMutable.value = stateMutable.value.copy(
                    status = PlaybackStatus.ERROR, error = "session_connection_failed",
                )
            }
        }, ContextCompat.getMainExecutor(context))
        scope.launch {
            while (true) {
                controller?.let(::update)
                delay(750)
            }
        }
    }

    private fun withController(block: (MediaController) -> Unit) {
        val active = controller
        if (active != null) block(active) else pending = block
    }

    override fun play(item: PlaybackItem) = playQueue(listOf(item), 0)

    override fun playQueue(items: List<PlaybackItem>, startIndex: Int) {
        require(items.isNotEmpty() && startIndex in items.indices)
        this.items = items
        restored = false
        stateMutable.value = UnifiedPlaybackState(
            status = PlaybackStatus.PREPARING, currentItem = items[startIndex],
            queue = items, currentIndex = startIndex, playWhenReady = true,
        )
        withController { player ->
            player.setMediaItems(items.map(PlaybackMediaItemMapper::toMediaItem), startIndex, 0L)
            scope.launch {
                val item = items[startIndex]
                if (!item.isLive) {
                    val saved = kotlinx.coroutines.withContext(Dispatchers.IO) { progressDao.get(item.id) }
                    if (saved != null && !saved.completed) player.seekTo(saved.positionMs)
                }
                player.prepare()
                player.play()
            }
        }
    }

    override fun pause() { withController { it.pause() } }
    override fun resume() { withController { it.play() } }
    override fun stop() {
        pending = null
        withController { it.stop(); it.clearMediaItems() }
        items = emptyList()
        restored = false
        stateMutable.value = UnifiedPlaybackState()
    }
    override fun seekTo(positionMs: Long) {
        if (stateMutable.value.currentItem?.isLive == false) withController { it.seekTo(positionMs.coerceAtLeast(0)) }
    }
    override fun next() { withController { if (it.hasNextMediaItem()) it.seekToNextMediaItem() } }
    override fun previous() { withController { if (it.hasPreviousMediaItem()) it.seekToPreviousMediaItem() } }
    override fun setPlaybackSpeed(speed: Float) {
        if (stateMutable.value.currentItem?.isLive == false) withController { it.setPlaybackSpeed(speed.coerceIn(0.5f, 2f)) }
    }
    override fun setRepeatMode(repeatMode: Int) { withController { it.repeatMode = repeatMode } }
    override fun setShuffle(enabled: Boolean) { withController { it.shuffleModeEnabled = enabled } }
    override fun setSleepTimer(minutes: Int?) {
        require(minutes == null || minutes == -1 || minutes in 1..180)
        withController {
            appContext.startService(Intent(appContext, UnifiedPlaybackService::class.java).apply {
                action = UnifiedPlaybackService.ACTION_SLEEP_TIMER
                putExtra(UnifiedPlaybackService.EXTRA_MINUTES, minutes ?: 0)
            })
        }
    }

    private fun update(player: Player) {
        val index = player.currentMediaItemIndex
        val item = items.getOrNull(index)
        val status = when {
            player.playerError != null -> PlaybackStatus.ERROR
            player.playbackState == Player.STATE_BUFFERING -> PlaybackStatus.BUFFERING
            player.playbackState == Player.STATE_ENDED -> PlaybackStatus.ENDED
            player.isPlaying -> PlaybackStatus.PLAYING
            player.playbackState == Player.STATE_READY -> PlaybackStatus.PAUSED
            player.playbackState == Player.STATE_IDLE && item != null ->
                if (restored) PlaybackStatus.PAUSED else PlaybackStatus.PREPARING
            else -> PlaybackStatus.IDLE
        }
        stateMutable.value = stateMutable.value.copy(
            status = status, currentItem = item,
            positionMs = if (item?.isLive == true) 0 else player.currentPosition.coerceAtLeast(0),
            durationMs = player.duration.takeIf { it != C.TIME_UNSET && it > 0 && item?.isLive != true },
            bufferedPositionMs = player.bufferedPosition.coerceAtLeast(0),
            playWhenReady = player.playWhenReady,
            error = if (status == PlaybackStatus.ERROR) stateMutable.value.error else null,
            queue = items, currentIndex = if (item == null) -1 else index,
        )
    }
}
