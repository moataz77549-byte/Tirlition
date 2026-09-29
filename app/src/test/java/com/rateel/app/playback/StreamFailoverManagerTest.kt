package com.rateel.app.playback

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class StreamFailoverManagerTest {
    @Test fun boundedBackoffThenBackup() {
        val manager = StreamFailoverManager()
        assertEquals(RetryDecision(0, 2_000), manager.next(2))
        assertEquals(RetryDecision(0, 5_000), manager.next(2))
        assertEquals(RetryDecision(0, 10_000), manager.next(2))
        assertEquals(RetryDecision(1, 2_000), manager.next(2))
        manager.next(2); manager.next(2)
        assertNull(manager.next(2))
    }
    @Test fun readyResetsFailures() {
        val manager = StreamFailoverManager()
        manager.next(1); manager.onReady()
        assertEquals(RetryDecision(0, 2_000), manager.next(1))
    }
}
