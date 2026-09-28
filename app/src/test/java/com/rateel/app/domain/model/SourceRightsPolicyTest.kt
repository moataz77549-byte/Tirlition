package com.rateel.app.domain.model

import org.junit.Assert.*
import org.junit.Test

class SourceRightsPolicyTest {
    private fun verifiedSource(
        allowStreaming: Boolean = true,
        allowDownload: Boolean = false,
        allowOfflinePlayback: Boolean = false,
        allowCaching: Boolean = false,
        allowOfflineSync: Boolean = false,
        allowRecording: Boolean = false,
    ) = ContentSource(
        id = "test-source",
        name = "Test",
        provider = "Test",
        type = SourceType.API,
        rightsStatus = SourceRightsStatus.VERIFIED_ALLOWED,
        isVerified = true,
        allowStreaming = allowStreaming,
        allowDownload = allowDownload,
        allowOfflinePlayback = allowOfflinePlayback,
        allowCaching = allowCaching,
        allowOfflineSync = allowOfflineSync,
        allowRecording = allowRecording,
    )

    @Test fun unverified_source_denies_even_explicit_permission() {
        assertFalse(SourceRightsPolicy.evaluate(verifiedSource().copy(isVerified = false), RightsAction.STREAM).allowed)
    }
    @Test fun recording_is_independent_from_streaming_permission() {
        val source = verifiedSource(allowStreaming = true, allowRecording = false)
        assertTrue(SourceRightsPolicy.evaluate(source, RightsAction.STREAM).allowed)
        assertFalse(SourceRightsPolicy.evaluate(source, RightsAction.RECORD).allowed)
    }
    @Test fun remote_switch_can_disable_capability_without_app_release() {
        assertFalse(SourceRightsPolicy.evaluate(verifiedSource().copy(streamingEnabled = false), RightsAction.STREAM).allowed)
    }
    @Test fun offline_sync_and_cache_are_independent_business_rules() {
        val source = verifiedSource(allowCaching = true, allowOfflineSync = true, allowOfflinePlayback = true)
        assertTrue(SourceRightsPolicy.evaluate(source, RightsAction.CACHE).allowed)
        assertTrue(SourceRightsPolicy.evaluate(source, RightsAction.OFFLINE_SYNC).allowed)
        assertTrue(SourceRightsPolicy.evaluate(source, RightsAction.OFFLINE_PLAYBACK).allowed)
        assertFalse(SourceRightsPolicy.evaluate(source, RightsAction.DOWNLOAD).allowed)
    }
    @Test fun pending_asset_reduces_download_without_disabling_stream() {
        val caps = ContentCapabilityResolver.resolve(
            verifiedSource(allowStreaming = true, allowDownload = true, allowOfflinePlayback = true),
            ContentType.SURAH_AUDIO,
            AssetRightsStatus.PENDING_VERIFICATION,
        )
        assertTrue(caps.canStream)
        assertFalse(caps.canDownload)
        assertFalse(caps.canKeepOffline)
    }
}
