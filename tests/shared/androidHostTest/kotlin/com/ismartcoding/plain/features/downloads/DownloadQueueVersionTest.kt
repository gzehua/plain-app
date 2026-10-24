package com.ismartcoding.plain.features.downloads

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertFailsWith

class DownloadQueueVersionTest {
    @Test
    fun restartAcceptsRevisionZeroWhileSameRuntimeRejectsDuplicateAndOlderUpdates() {
        val version = DownloadQueueVersion()
        assertTrue(version.accept("first", 50))
        assertFalse(version.accept("first", 49))
        assertFalse(version.accept("first", 50))
        assertTrue(version.accept("second", 0))
        assertEquals("second", version.runtimeId)
        assertEquals(0L, version.revision)
        assertFalse(version.accept("second", 0))
        assertTrue(version.accept("second", 1))
    }
    @Test
    fun invalidSnapshotCannotReplaceCurrentVersion() {
        val version = DownloadQueueVersion()
        version.accept("first", 8)
        assertFailsWith<IllegalArgumentException> { version.accept("second", -1) }
        assertEquals("first", version.runtimeId)
        assertEquals(8L, version.revision)
    }
}
