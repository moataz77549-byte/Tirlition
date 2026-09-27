package com.rateel.app.domain.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SourceRightsPolicyTest {
    private fun verifiedSource(
        allowStreaming: Boolean = true,
        allowDownload: Boolean = false,
        allowOfflinePlayback: Boolean = false,
        allowRecording: Boolean = false,
    ) = ContentSource(
        id = "test-source",
        name = "Test",
        provider = "Test",
        type = SourceType.API,
        isVerified = true,
        allowStreaming = allowStreaming,
        allowDownload = allowDownload,
        allowOfflinePlayback = allowOfflinePlayback,
        allowRecording = allowRecording,
    )

    @Test
    fun unverified_source_denies_even_explicit_permission() {
        val source = verifiedSource().copy(isVerified = false)
        assertFalse(SourceRightsPolicy.evaluate(source, RightsAction.STREAM).allowed)
    }

    @Test
    fun recording_is_independent_from_streaming_permission() {
        val source = verifiedSource(allowStreaming = true, allowRecording = false)
        assertTrue(SourceRightsPolicy.evaluate(source, RightsAction.STREAM).allowed)
        assertFalse(SourceRightsPolicy.evaluate(source, RightsAction.RECORD).allowed)
    }

    @Test
    fun remote_switch_can_disable_capability_without_app_release() {
        val source = verifiedSource().copy(streamingEnabled = false)
        assertFalse(SourceRightsPolicy.evaluate(source, RightsAction.STREAM).allowed)
    }

    @Test
    fun offline_playback_requires_download_and_offline_rights() {
        val source = verifiedSource(
            allowDownload = true,
            allowOfflinePlayback = true,
        )
        assertTrue(SourceRightsPolicy.evaluate(source, RightsAction.OFFLINE_PLAYBACK).allowed)
        assertFalse(
            SourceRightsPolicy.evaluate(
                source.copy(allowDownload = false),
                RightsAction.OFFLINE_PLAYBACK,
            ).allowed,
        )
    }
}
