package com.ismartcoding.plain.features.audio

import com.ismartcoding.plain.api.graphql.models.MediaLibraryChange
import com.ismartcoding.plain.api.graphql.models.MediaScanProgress
import com.ismartcoding.plain.rust.graphql.SubscriptionUpdate
import kotlin.test.*

class AudioOverviewClientTest {
    @Test fun only_audio_changes_and_terminal_scan_states_refresh() {
        assertTrue(audioLibraryChangeRefreshes(SubscriptionUpdate.Attached))
        assertTrue(audioLibraryChangeRefreshes(SubscriptionUpdate.Data(MediaLibraryChange(listOf(1)))))
        assertFalse(audioLibraryChangeRefreshes(SubscriptionUpdate.Data(MediaLibraryChange(listOf(3)))))
        assertFalse(audioScanChangeRefreshes(SubscriptionUpdate.Data(MediaScanProgress(indexed = 0, state = "RUNNING"))))
        for (state in listOf("IDLE", "FAILED", "STOPPED", "PAUSED")) {
            assertTrue(audioScanChangeRefreshes(SubscriptionUpdate.Data(MediaScanProgress(indexed = 0, state = state))))
        }
    }
}
