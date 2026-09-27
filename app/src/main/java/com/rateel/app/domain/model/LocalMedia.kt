package com.rateel.app.domain.model

import com.rateel.app.playback.RecordingMode

data class LocalRecording(
    val id: String,
    val stationId: String,
    val sourceId: String,
    val stationName: String,
    val title: String,
    val filePath: String,
    val mimeType: String,
    val durationMs: Long?,
    val fileSizeBytes: Long?,
    val startedAt: Long,
    val finishedAt: Long?,
    val recordingMode: RecordingMode,
    val requestedDurationMs: Long?,
    val artwork: String?,
    val sourceAttribution: String?,
    val createdAt: Long,
)

data class DownloadedAudio(
    val id: String,
    val sourceId: String,
    val remoteUrl: String,
    val localUri: String?,
    val contentType: String,
    val reciterId: String?,
    val mushafId: String?,
    val surahNumber: Int?,
    val fileSizeBytes: Long?,
    val checksum: String?,
    val downloadedAt: Long?,
    val rightsSnapshot: String,
)
