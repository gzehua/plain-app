package com.ismartcoding.plain.features.mediaactions

import com.ismartcoding.plain.TempData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class MediaActionProjectionTest {
    @Test
    fun deletionInvalidatesOnlyCommittedPlaybackItems() {
        val progress = TempData.videoPlayProgressMap.toMap()
        try {
            TempData.videoPlayProgressMap["removed"] = 40
            TempData.videoPlayProgressMap["failed"] = 50
            MediaActionProjection.apply("""{"type":2,"action":"DELETE","ids":["removed"]}""")
            assertNull(TempData.videoPlayProgressMap["removed"])
            assertEquals(50L, TempData.videoPlayProgressMap["failed"])
            MediaActionProjection.apply("""{"type":2,"action":"RESTORE","ids":["failed"]}""")
            assertEquals(50L, TempData.videoPlayProgressMap["failed"])
        } finally {
            TempData.videoPlayProgressMap.clear()
            TempData.videoPlayProgressMap.putAll(progress)
        }
    }
}
