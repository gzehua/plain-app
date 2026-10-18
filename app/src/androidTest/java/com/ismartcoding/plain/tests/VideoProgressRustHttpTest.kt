package com.ismartcoding.plain.tests

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ismartcoding.plain.features.videos.VideoProgressHelper
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days

@RunWith(AndroidJUnit4::class)
class VideoProgressRustHttpTest {
    @Test
    fun videoPositionUsesRustAndRetainsLongMilliseconds() = runBlocking {
        val id = "video-rust-${UUID.randomUUID()}"
        val since = Clock.System.now() - 1.days
        suspend fun current() = VideoProgressHelper.recentAsync(since).single { it.mediaId == id }
        val row = VideoProgressHelper.saveAsync(id, 3_000_000_123)
        assertEquals(3_000_000_123, current().positionMs)
        assertEquals(row.updatedAt, current().updatedAt)
        assertTrue(runCatching { VideoProgressHelper.saveAsync(id, -1) }.isFailure)
        assertEquals(3_000_000_123, current().positionMs)
        VideoProgressHelper.recordPlayAsync(id, 456)
        assertEquals(456L, current().positionMs)
        VideoProgressHelper.saveAsync(id, 0)
        assertEquals(0L, current().positionMs)
    }
}
