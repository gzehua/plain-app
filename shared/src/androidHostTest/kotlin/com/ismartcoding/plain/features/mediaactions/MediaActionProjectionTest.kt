package com.ismartcoding.plain.features.mediaactions

import com.ismartcoding.plain.TempData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class MediaActionProjectionTest {
    private fun duration(value: Long) = com.ismartcoding.plain.graphql.models.MediaHostDuration(
        com.ismartcoding.plain.enums.DataType.VIDEO, "id", value,
        kotlin.time.Instant.fromEpochSeconds(0), "/file", 1, kotlin.time.Instant.fromEpochSeconds(0),
    )
    @Test
    fun deletionInvalidatesOnlyCommittedItemsAndLeavesOtherKindsAlone() {
        val durations = TempData.mediaDurationMap.toMap()
        val progress = TempData.videoPlayProgressMap.toMap()
        try {
            TempData.mediaDurationMap["video:removed"] = duration(100)
            TempData.mediaDurationMap["video:failed"] = duration(200)
            TempData.mediaDurationMap["audio:removed"] = duration(300)
            TempData.videoPlayProgressMap["removed"] = 40
            TempData.videoPlayProgressMap["failed"] = 50
            MediaActionProjection.apply("""{"type":2,"action":"DELETE","ids":["removed"]}""")
            assertNull(TempData.mediaDurationMap["video:removed"])
            assertNull(TempData.videoPlayProgressMap["removed"])
            assertEquals(200L, TempData.mediaDurationMap["video:failed"]?.durationMs)
            assertEquals(50L, TempData.videoPlayProgressMap["failed"])
            assertEquals(300L, TempData.mediaDurationMap["audio:removed"]?.durationMs)
            MediaActionProjection.apply("""{"type":2,"action":"RESTORE","ids":["failed"]}""")
            assertNull(TempData.mediaDurationMap["video:failed"])
        } finally {
            TempData.mediaDurationMap.clear()
            TempData.mediaDurationMap.putAll(durations)
            TempData.videoPlayProgressMap.clear()
            TempData.videoPlayProgressMap.putAll(progress)
        }
    }
}
