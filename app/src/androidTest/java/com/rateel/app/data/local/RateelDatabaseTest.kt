package com.rateel.app.data.local

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RateelDatabaseTest {
    private lateinit var database: RateelDatabase

    @Before
    fun createDatabase() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, RateelDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun source_dao_persists_rights_metadata() = runBlocking {
        val source = ContentSourceEntity(
            id = "test",
            name = "Test",
            provider = "Provider",
            type = "API",
            website = null,
            apiBaseUrl = null,
            documentationUrl = null,
            termsUrl = null,
            copyrightUrl = null,
            attributionText = null,
            licenseType = "UNKNOWN",
            requiresAttribution = false,
            allowStreaming = true,
            allowDownload = false,
            allowOfflinePlayback = false,
            allowCaching = false,
            allowOfflineSync = false,
            allowRecording = false,
            allowSharing = false,
            allowCommercialUse = false,
            maxOfflineRetentionDays = null,
            requiresPeriodicSync = false,
            isOfficial = false,
            isVerified = true,
            lastRightsCheckAt = 1L,
            lastTechnicalCheckAt = 2L,
            notes = null,
            isEnabled = true,
            streamingEnabled = true,
            downloadEnabled = false,
            offlinePlaybackEnabled = false,
            cachingEnabled = false,
            offlineSyncEnabled = false,
            recordingEnabled = false,
            sharingEnabled = false,
            disabledReason = null,
        )

        database.sourceDao().insertIfMissing(listOf(source))
        val stored = database.sourceDao().observeAll().first().single()

        assertEquals("test", stored.id)
        assertEquals(true, stored.allowStreaming)
        assertEquals(1L, stored.lastRightsCheckAt)
    }
}
