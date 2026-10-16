package com.ismartcoding.plain.tests

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ismartcoding.plain.features.clipboard.ClipboardClient
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class ClipboardRustHttpTest {
    @Test
    fun historyUsesRustAndDeduplicatesWithoutWritingRoom() = runBlocking {
        val marker = "clipboard-rust-${UUID.randomUUID()}"
        val text = "$marker 🌍 100%_ \\ \"quoted\""
        val ids = mutableListOf<String>()
        try {
            val entry = checkNotNull(ClipboardClient.record(text, source = marker, label = "label-$marker", sensitive = true))
            ids += entry.id
            assertEquals(text, entry.text)
            assertEquals(marker, entry.source)
            assertEquals("label-$marker", entry.label)
            assertTrue(entry.sensitive)
            assertNull(ClipboardClient.record(text))
            assertEquals(1, ClipboardClient.count(marker))
            assertEquals(entry, ClipboardClient.getPage(50, 0, marker).single())
            assertTrue(ClipboardClient.getPage(50, 1, marker).isEmpty())
            assertEquals(0, ClipboardClient.deleteByIds(emptyList()))
            assertEquals(1, ClipboardClient.deleteByIds(ids))
            assertEquals(0, ClipboardClient.count(marker))
        } finally {
            ClipboardClient.deleteByIds(ids)
        }
    }
}
