@file:OptIn(ExperimentalForeignApi::class)

package com.ismartcoding.plain.platform.media

import kotlinx.cinterop.readValue
import kotlin.test.Test
import kotlin.test.assertEquals
import platform.CoreMedia.CMTimeMake
import platform.CoreMedia.kCMTimeIndefinite
import platform.CoreMedia.kCMTimeInvalid
import platform.CoreMedia.kCMTimeNegativeInfinity
import platform.CoreMedia.kCMTimePositiveInfinity
import kotlinx.cinterop.ExperimentalForeignApi

class MediaTimeTest {
    @Test
    fun preserves_milliseconds_and_large_positions() {
        assertEquals(0L, mediaTimeMs(CMTimeMake(0, 1000)))
        assertEquals(1234L, mediaTimeMs(CMTimeMake(1234, 1000)))
        assertEquals(3_000_000_123L, mediaTimeMs(CMTimeMake(3_000_000_123, 1000)))
        assertEquals(500L, mediaTimeMs(CMTimeMake(1, 2)))
    }

    @Test
    fun unavailable_or_negative_media_time_has_no_position() {
        assertEquals(0L, mediaTimeMs(CMTimeMake(-1, 1000)))
        assertEquals(0L, mediaTimeMs(kCMTimeInvalid.readValue()))
        assertEquals(0L, mediaTimeMs(kCMTimeIndefinite.readValue()))
        assertEquals(0L, mediaTimeMs(kCMTimePositiveInfinity.readValue()))
        assertEquals(0L, mediaTimeMs(kCMTimeNegativeInfinity.readValue()))
    }
}
