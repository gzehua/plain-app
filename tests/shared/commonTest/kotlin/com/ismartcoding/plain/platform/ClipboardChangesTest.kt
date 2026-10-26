package com.ismartcoding.plain.platform

import com.ismartcoding.plain.events.ClipboardReportData
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.*
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class ClipboardChangesTest {
    @Test
    fun background_changes_wait_for_foreground_and_repeated_notifications_read_once() = runTest {
        val changes = MutableSharedFlow<Unit>(extraBufferCapacity = 8)
        var active = false
        var revision = 1L
        var reads = 0
        val reports = mutableListOf<ClipboardReportData>()
        val observer = launch {
            forwardClipboardChanges(changes, { active }, { revision }, { reads++; "sample $revision" }, reports::add)
        }
        runCurrent()
        changes.emit(Unit)
        runCurrent()
        assertEquals(0, reads)
        active = true
        changes.emit(Unit)
        changes.emit(Unit)
        runCurrent()
        assertEquals(1, reads)
        assertEquals(listOf("sample 1"), reports.map { it.text })
        active = false
        revision++
        changes.emit(Unit)
        runCurrent()
        assertEquals(1, reads)
        active = true
        changes.emit(Unit)
        runCurrent()
        assertEquals(listOf("sample 1", "sample 2"), reports.map { it.text })
        assertTrue(reports.none { it.sensitive })
        observer.cancelAndJoin()
        revision++
        changes.emit(Unit)
        runCurrent()
        assertEquals(2, reads)
    }

    @Test
    fun unavailable_blank_and_oversized_samples_are_bounded_without_repeated_reads() = runTest {
        val changes = MutableSharedFlow<Unit>(extraBufferCapacity = 8)
        var revision = 0L
        var text: String? = null
        var reads = 0
        val reports = mutableListOf<ClipboardReportData>()
        val observer = launch {
            forwardClipboardChanges(changes, { true }, { revision }, { reads++; text }, reports::add)
        }
        runCurrent()
        for (value in listOf(null, " ", "🌍".repeat(128 * 1024 + 1), "🌍".repeat(128 * 1024))) {
            revision++
            text = value
            changes.emit(Unit)
            changes.emit(Unit)
            runCurrent()
        }
        assertEquals(4, reads)
        assertEquals(1, reports.size)
        assertEquals(256 * 1024, reports.single().text.length)
        observer.cancelAndJoin()
    }
}
