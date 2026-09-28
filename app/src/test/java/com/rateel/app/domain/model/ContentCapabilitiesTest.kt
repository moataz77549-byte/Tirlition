package com.rateel.app.domain.model

import com.rateel.app.domain.source.PlannedSourceCatalog
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ContentCapabilitiesTest {
    @Test fun thirdPartyAssetIsStreamingOnly() {
        val result = ContentCapabilityResolver.resolve(
            PlannedSourceCatalog.mp3QuranV3,
            ContentAsset(SourceIds.MP3_QURAN_V3, "third-party.example", SourceIds.MP3_QURAN_V3),
        )
        assertTrue(result.canStream)
        assertFalse(result.canDownload)
        assertFalse(result.canRecord)
        assertFalse(result.canKeepOffline)
    }

    @Test fun liveChannelCannotBeRecordedOrDownloaded() {
        val result = ContentCapabilityResolver.resolve(
            PlannedSourceCatalog.mp3QuranLiveTv,
            ContentAsset(SourceIds.MP3_QURAN_LIVE_TV, "win.holol.com",
                SourceIds.MP3_QURAN_V3, isLiveChannel = true),
        )
        assertTrue(result.canStream)
        assertFalse(result.canRecord)
        assertFalse(result.canDownload)
    }

    @Test fun disabledSourceCannotStream() {
        val result = ContentCapabilityResolver.resolve(
            PlannedSourceCatalog.quranFoundation,
            ContentAsset(SourceIds.QURAN_FOUNDATION, "apis.quran.foundation",
                SourceIds.QURAN_FOUNDATION),
        )
        assertFalse(result.canStream)
    }
}
