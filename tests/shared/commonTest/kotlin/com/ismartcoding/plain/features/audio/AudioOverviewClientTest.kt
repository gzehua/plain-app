package com.ismartcoding.plain.features.audio

import com.ismartcoding.plain.events.EventType
import kotlin.test.*

class AudioOverviewClientTest {
    @Test
    fun only_relevant_audio_changes_or_scan_boundaries_refresh_the_summary() {
        assertTrue(AudioOverviewClient.updatesOverview(EventType.AUDIO_LIBRARY_UPDATED, "{}"))
        assertTrue(AudioOverviewClient.updatesOverview(EventType.MEDIA_LIBRARY_UPDATED, """{"types":[1]}"""))
        assertFalse(AudioOverviewClient.updatesOverview(EventType.MEDIA_LIBRARY_UPDATED, """{"types":[3]}"""))
        assertFalse(AudioOverviewClient.updatesOverview(EventType.MEDIA_SCAN_PROGRESS, """{"state":"RUNNING"}"""))
        for (state in listOf("IDLE", "FAILED", "STOPPED", "PAUSED")) {
            assertTrue(AudioOverviewClient.updatesOverview(EventType.MEDIA_SCAN_PROGRESS, """{"state":"$state"}"""))
        }
        for (type in listOf(EventType.CONTENT_CHANGED, EventType.FEED_SYNC_UPDATED, EventType.PREFS_UPDATED, EventType.MEDIA_ACTION_FINISHED)) {
            assertFalse(AudioOverviewClient.updatesOverview(type, "must not parse unrelated payloads"))
        }
    }
}
