package com.ismartcoding.plain.features.media.actions

import com.ismartcoding.plain.ui.media.actions.MediaActionProjection

import com.ismartcoding.plain.ui.app.AppUiState

import com.ismartcoding.plain.app.TempData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class MediaActionProjectionTest {
    @Test
    fun deletionInvalidatesOnlyCommittedPlaybackItems() {
        val progress = AppUiState.videoPlayProgressMap.toMap()
        try {
            AppUiState.videoPlayProgressMap["removed"] = 40
            AppUiState.videoPlayProgressMap["failed"] = 50
            MediaActionProjection.apply("""{"type":2,"action":"DELETE","ids":["removed"]}""")
            assertNull(AppUiState.videoPlayProgressMap["removed"])
            assertEquals(50L, AppUiState.videoPlayProgressMap["failed"])
            MediaActionProjection.apply("""{"type":2,"action":"RESTORE","ids":["failed"]}""")
            assertEquals(50L, AppUiState.videoPlayProgressMap["failed"])
        } finally {
            AppUiState.videoPlayProgressMap.clear()
            AppUiState.videoPlayProgressMap.putAll(progress)
        }
    }
}
