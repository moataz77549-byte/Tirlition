package com.rateel.app.domain.model

import com.rateel.app.domain.source.PlannedSourceCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaybackItemTest {
    @Test fun radioIdentityIsStationIdNotStreamUrl() {
        val source = PlannedSourceCatalog.qurangoStreams
        val endpoint = StreamEndpoint(source.id, "https://backup.qurango.net/radio/test")
        val caps = ContentCapabilityResolver.resolve(source,
            ContentAsset(source.id, "backup.qurango.net", SourceIds.MP3_QURAN_V3))
        val item = RadioStation(
            id = "mp3quran:radio:5", sourceId = SourceIds.MP3_QURAN_V3,
            canonicalKey = "mp3quran:5", nameArabic = "إذاعة",
            streams = listOf(endpoint),
        ).toPlaybackItem(endpoint, caps)
        assertEquals("mp3quran:radio:5", item.id)
        assertEquals("mp3quran:radio:5", item.stationId)
        assertTrue(item.isLive)
        assertFalse(item.capabilities.canRecord)
    }
}
