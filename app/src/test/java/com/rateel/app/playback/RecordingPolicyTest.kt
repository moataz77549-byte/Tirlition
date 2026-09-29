package com.rateel.app.playback

import com.rateel.app.domain.model.ContentSource
import com.rateel.app.domain.model.SourceIds
import com.rateel.app.domain.model.SourceType
import com.rateel.app.domain.model.StreamEndpoint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RecordingPolicyTest {
    private val source = ContentSource(id = SourceIds.QURANGO_STREAMS,
        name = "Qurango", provider = "MP3Quran", type = SourceType.CONTENT_PROVIDER,
        isVerified = true, allowStreaming = true)
    private val stream = StreamEndpoint(source.id, "https://backup.qurango.net/radio", "mp3")
    @Test fun deniedRightsCannotBeBypassed() {
        assertTrue(StreamRecordingPolicy.canRecord(source, stream) is RecordingCapability.Unsupported)
        assertTrue(StreamRecordingPolicy.canRecord(source.copy(allowRecording = true, recordingEnabled = false), stream)
            is RecordingCapability.Unsupported)
    }
    @Test fun hostAndFormatGate() {
        val permitted = source.copy(allowRecording = true)
        assertEquals(RecordingCapability.Supported, StreamRecordingPolicy.canRecord(permitted, stream))
        assertTrue(StreamRecordingPolicy.canRecord(permitted, stream.copy(url = "https://unknown.example/live"))
            is RecordingCapability.Unsupported)
        assertTrue(StreamRecordingPolicy.canRecord(permitted, stream.copy(format = "hls"))
            is RecordingCapability.Unsupported)
    }
    @Test fun safeNameAndDurations() {
        assertEquals("rateel_recording_mp3quran_radio_23_42.aac",
            RecordingFileNames.create("mp3quran:radio/23", 42, "aac"))
        assertEquals(listOf(5, 10, 15, 30), RecordingDurations.supportedMinutes)
    }
}
