package com.ismartcoding.plain.features.mediaactions

import com.ismartcoding.plain.TempData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class MediaActionProjectionTest {
    @Test
    fun deletionInvalidatesOnlyCommittedItemsAndLeavesOtherKindsAlone() {
        val durations = TempData.mediaDurationMap.toMap()
        val progress = TempData.videoPlayProgressMap.toMap()
        try {
            TempData.mediaDurationMap["video:removed"] = 100
            TempData.mediaDurationMap["video:failed"] = 200
            TempData.mediaDurationMap["audio:removed"] = 300
            TempData.videoPlayProgressMap["removed"] = 40
            TempData.videoPlayProgressMap["failed"] = 50
            MediaActionProjection.apply("""{"type":2,"action":"DELETE","ids":["removed"]}""")
            assertNull(TempData.mediaDurationMap["video:removed"])
            assertNull(TempData.videoPlayProgressMap["removed"])
            assertEquals(200L, TempData.mediaDurationMap["video:failed"])
            assertEquals(50L, TempData.videoPlayProgressMap["failed"])
            assertEquals(300L, TempData.mediaDurationMap["audio:removed"])
            MediaActionProjection.apply("""{"type":2,"action":"RESTORE","ids":["failed"]}""")
            assertEquals(200L, TempData.mediaDurationMap["video:failed"])
        } finally {
            TempData.mediaDurationMap.clear()
            TempData.mediaDurationMap.putAll(durations)
            TempData.videoPlayProgressMap.clear()
            TempData.videoPlayProgressMap.putAll(progress)
        }
    }
}
