package com.ismartcoding.plain.tests

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class BookmarkRustHttpTest {
    @Test
    fun bookmarksUseRustAndKeepClicksWhenEditing() = runBlocking {
        val marker = "bookmark-rust-${UUID.randomUUID()}"
        val group = TestBookmarksClient.createGroup(marker)
        val ids = mutableSetOf<String>()
        try {
            val bookmark = TestBookmarksClient.addBookmarks(listOf("https://example.invalid/$marker", " "), group.id).single()
            ids += bookmark.id
            coroutineScope { (0 until 12).map { async { TestBookmarksClient.recordClick(bookmark.id) } }.awaitAll() }
            val updated = checkNotNull(TestBookmarksClient.updateBookmark(bookmark.id) { title = marker; pinned = true; sortOrder = 7 })
            assertEquals(12, updated.clickCount)
            assertNotNull(updated.lastClickedAt)
            assertEquals(marker, updated.title)
            assertTrue(updated.pinned)
            assertEquals(7, updated.sortOrder)
            assertEquals(1, TestBookmarksClient.groupItemCounts()[group.id])
            assertTrue(checkNotNull(TestBookmarksClient.updateGroup(group.id) { collapsed = true }).collapsed)
            TestBookmarksClient.deleteGroup(group.id)
            assertNull(TestBookmarksClient.getGroupById(group.id))
            assertEquals("", checkNotNull(TestBookmarksClient.getById(bookmark.id)).groupId)
            assertEquals(0, TestBookmarksClient.deleteBookmarks(emptySet()))
            assertEquals(1, TestBookmarksClient.deleteBookmarks(ids))
            assertNull(TestBookmarksClient.getById(bookmark.id))
        } finally {
            TestBookmarksClient.deleteBookmarks(ids)
            TestBookmarksClient.deleteGroup(group.id)
        }
    }
}
