package com.rateel.app.playback

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.rateel.app.R
import com.rateel.app.domain.model.StreamEndpoint
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/** User initiated foreground transfer, separate from the playback MediaSession. */
@AndroidEntryPoint
class RecordingService : Service() {
    @Inject lateinit var recorder: StreamRecorder
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var stationName = ""
    private var lastNotificationSecond = -1L
    private var sessionStarted = false
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(
            NotificationChannel(CHANNEL, getString(R.string.recordings), NotificationManager.IMPORTANCE_LOW))
        scope.launch {
            recorder.state.collect { state ->
                if (sessionStarted && (state.status == RecordingStatus.RECORDING || state.status == RecordingStatus.FINALIZING)
                    && state.elapsedMs / 1000 != lastNotificationSecond) {
                    lastNotificationSecond = state.elapsedMs / 1000
                    (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).notify(ID,
                        notification(state.elapsedMs / 1000))
                }
                if (sessionStarted && state.status == RecordingStatus.COMPLETED) {
                    (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).notify(ID + 1,
                        NotificationCompat.Builder(this@RecordingService, CHANNEL)
                            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
                            .setContentTitle(getString(R.string.recording_saved))
                            .setContentText(stationName).setAutoCancel(true).build())
                }
                if (sessionStarted && state.status in setOf(
                        RecordingStatus.COMPLETED, RecordingStatus.FAILED, RecordingStatus.CANCELLED))
                    stopSelf()
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                if (recorder.state.value.status in setOf(RecordingStatus.PREPARING,
                        RecordingStatus.RECORDING, RecordingStatus.FINALIZING)) return START_NOT_STICKY
                sessionStarted = true
                stationName = intent.getStringExtra(EXTRA_NAME).orEmpty()
                val id = intent.getStringExtra(EXTRA_STATION) ?: return START_NOT_STICKY
                val source = intent.getStringExtra(EXTRA_SOURCE) ?: return START_NOT_STICKY
                val url = intent.getStringExtra(EXTRA_URL) ?: return START_NOT_STICKY
                val format = intent.getStringExtra(EXTRA_FORMAT)
                val minutes = intent.getIntExtra(EXTRA_MINUTES, 0)
                if (Build.VERSION.SDK_INT >= 29)
                    startForeground(ID, notification(0), ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
                else startForeground(ID, notification(0))
                scope.launch {
                    val mode = if (minutes == 0) RecordingMode.MANUAL else RecordingMode.FIXED_DURATION
                    val result = recorder.start(RecordingRequest(id, stationName,
                        StreamEndpoint(source, url, format), mode,
                        if (minutes == 0) null else minutes * 60_000L))
                    if (result.isFailure) stopSelf()
                }
            }
            ACTION_STOP -> scope.launch { recorder.stop(); stopSelf() }
            ACTION_CANCEL -> scope.launch { recorder.cancel(); stopSelf() }
        }
        return START_NOT_STICKY
    }

    private fun notification(seconds: Long): Notification {
        val stopIntent = Intent(this, RecordingService::class.java).setAction(ACTION_STOP)
        val stop = PendingIntent.getService(this, 0, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        return NotificationCompat.Builder(this, CHANNEL)
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentTitle(getString(R.string.recording_active))
            .setContentText("$stationName · ${seconds / 60}:${(seconds % 60).toString().padStart(2, '0')}")
            .setOngoing(true)
            .addAction(0, getString(R.string.stop_recording), stop)
            .build()
    }

    override fun onDestroy() {
        scope.launch {
            if (recorder.state.value.status in setOf(RecordingStatus.PREPARING, RecordingStatus.RECORDING))
                recorder.cancel()
            scope.cancel()
        }
        super.onDestroy()
    }

    companion object {
        const val ACTION_START = "com.rateel.app.RECORD_START"
        const val ACTION_STOP = "com.rateel.app.RECORD_STOP"
        const val ACTION_CANCEL = "com.rateel.app.RECORD_CANCEL"
        const val EXTRA_STATION = "station"
        const val EXTRA_NAME = "name"
        const val EXTRA_SOURCE = "source"
        const val EXTRA_URL = "url"
        const val EXTRA_FORMAT = "format"
        const val EXTRA_MINUTES = "minutes"
        private const val CHANNEL = "rateel_recording"
        private const val ID = 2201
    }
}
