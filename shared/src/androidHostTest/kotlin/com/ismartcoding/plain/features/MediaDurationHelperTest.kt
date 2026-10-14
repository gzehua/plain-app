package com.ismartcoding.plain.features

import com.ismartcoding.plain.TempData
import com.ismartcoding.plain.enums.DataType
import com.ismartcoding.plain.graphql.models.MediaHostDuration
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class MediaDurationHelperTest {
    @Test
    fun cachedDurationRequiresMatchingFileIdentityAndMediaType() {
        val previous = TempData.mediaDurationMap.toMap()
        val modifiedAt = Instant.fromEpochSeconds(100)
        try {
            TempData.mediaDurationMap["video:same"] = MediaHostDuration(
                DataType.VIDEO, "same", 5_000_000_001L, modifiedAt, "/video", 42, modifiedAt,
            )
            assertEquals(5_000_000_001L, MediaDurationHelper.cachedDuration("video", "same", "/video", 42, modifiedAt))
            assertEquals(0L, MediaDurationHelper.cachedDuration("audio", "same", "/video", 42, modifiedAt))
            assertEquals(0L, MediaDurationHelper.cachedDuration("video", "same", "/replaced", 42, modifiedAt))
            assertEquals(0L, MediaDurationHelper.cachedDuration("video", "same", "/video", 43, modifiedAt))
            assertEquals(0L, MediaDurationHelper.cachedDuration("video", "same", "/video", 42, Instant.fromEpochSeconds(101)))
        } finally {
            TempData.mediaDurationMap.clear()
            TempData.mediaDurationMap.putAll(previous)
        }
    }
}
