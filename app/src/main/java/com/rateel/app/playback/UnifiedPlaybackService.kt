package com.rateel.app.playback

import android.content.Intent
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.rateel.app.data.local.ListeningHistoryDao
import com.rateel.app.data.local.ListeningHistoryEntity
import com.rateel.app.data.local.PlaybackProgressDao
import com.rateel.app.data.local.PlaybackProgressEntity
import com.rateel.app.domain.repository.RadioRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first

/** MediaSessionService owns the sole ExoPlayer, including when the UI is gone. */
@AndroidEntryPoint
class UnifiedPlaybackService : MediaSessionService() {
    @Inject lateinit var progressDao: PlaybackProgressDao
    @Inject lateinit var historyDao: ListeningHistoryDao
    @Inject lateinit var radios: RadioRepository
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var session: MediaSession? = null
    private var player: ExoPlayer? = null
    private var timer: Job? = null
    private var previousId: String? = null
    private var previousLive = false
    private var previousPosition = 0L
    private val failover = StreamFailoverManager()
    private var retryJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        val exo = ExoPlayer.Builder(this).build().apply {
            setAudioAttributes(AudioAttributes.Builder().setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC).build(), true)
            setHandleAudioBecomingNoisy(true)
        }
        player = exo
        exo.addListener(object : Player.Listener {
            override fun onEvents(player: Player, events: Player.Events) {
                val item = player.currentMediaItem
                val id = item?.mediaId
                if (previousId != null && id != previousId && !previousLive)
                    persist(previousId!!, previousPosition, false)
                if (id != null && id != previousId) {
                    retryJob?.cancel()
                    failover.reset()
                    previousId = id
                    previousLive = item.mediaMetadata.extras?.getBoolean("rateel.isLive") == true
                    previousPosition = 0
                    scope.launch(Dispatchers.IO) {
                        val prior = historyDao.latest("audio", id)
                        historyDao.upsert(ListeningHistoryEntity(id = prior?.id ?: 0,
                            contentType = "audio", contentId = id, playedAt = System.currentTimeMillis(),
                            titleSnapshot = item.mediaMetadata.title?.toString().orEmpty(),
                            sourceId = item.mediaMetadata.extras?.getString("rateel.sourceId")))
                    }
                }
                if (id != null && !previousLive && player.currentPosition > 0) {
                    previousPosition = player.currentPosition
                    if (!player.isPlaying || player.playbackState == Player.STATE_ENDED)
                        persist(id, previousPosition, player.playbackState == Player.STATE_ENDED)
                }
            }
            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                val item = exo.currentMediaItem ?: return
                if (item.mediaMetadata.extras?.getBoolean("rateel.isLive") != true) return
                retryJob?.cancel()
                retryJob = scope.launch {
                    // Resolve by stable station ID so a changed stream URL is never persisted forever.
                    runCatching { radios.refresh() }
                    val endpoints = radios.observeRadios().first()
                        .firstOrNull { it.id == item.mediaId }?.streams.orEmpty()
                    val decision = failover.next(endpoints.size) ?: return@launch
                    delay(decision.delayMs)
                    if (exo.currentMediaItem?.mediaId != item.mediaId) return@launch
                    val selected = endpoints[decision.endpointIndex]
                    val replacement = item.buildUpon().setUri(selected.url)
                        .setMimeType(if (selected.format == "hls" || selected.format == "m3u8")
                            "application/x-mpegURL" else null).build()
                    exo.replaceMediaItem(exo.currentMediaItemIndex, replacement)
                    exo.prepare()
                    exo.play()
                }
            }
        })
        scope.launch {
            while (true) {
                delay(10_000)
                val id = exo.currentMediaItem?.mediaId
                if (id != null && !previousLive && exo.currentPosition > 0) {
                    previousPosition = exo.currentPosition
                    persist(id, previousPosition, false)
                }
            }
        }
        session = MediaSession.Builder(this, exo).build()
    }

    private fun persist(id: String, position: Long, completed: Boolean) {
        val duration = player?.duration?.takeIf { it > 0 && it != C.TIME_UNSET }
        scope.launch(Dispatchers.IO) {
            progressDao.upsert(PlaybackProgressEntity(id, position.coerceAtLeast(0),
                duration, System.currentTimeMillis(), completed))
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_SLEEP_TIMER) {
            timer?.cancel()
            val minutes = intent.getIntExtra(EXTRA_MINUTES, 0)
            if (minutes in 1..180) timer = scope.launch {
                delay(minutes * 60_000L)
                player?.pause()
            }
            return START_STICKY
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = session

    override fun onDestroy() {
        timer?.cancel()
        retryJob?.cancel()
        session?.release()
        player?.release()
        session = null
        player = null
        scope.cancel()
        super.onDestroy()
    }

    companion object {
        const val ACTION_SLEEP_TIMER = "com.rateel.app.SLEEP_TIMER"
        const val EXTRA_MINUTES = "minutes"
    }
}
