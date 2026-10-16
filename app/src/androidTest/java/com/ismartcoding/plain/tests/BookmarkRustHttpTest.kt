package com.ismartcoding.plain.tests

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ismartcoding.plain.features.bookmarks.BookmarksClient
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
        val group = BookmarksClient.createGroup(marker)
        val ids = mutableSetOf<String>()
        try {
            val bookmark = BookmarksClient.addBookmarks(listOf("https://example.invalid/$marker", " "), group.id).single()
            ids += bookmark.id
            coroutineScope { (0 until 12).map { async { BookmarksClient.recordClick(bookmark.id) } }.awaitAll() }
            val updated = checkNotNull(BookmarksClient.updateBookmark(bookmark.id) { title = marker; pinned = true; sortOrder = 7 })
            assertEquals(12, updated.clickCount)
            assertNotNull(updated.lastClickedAt)
            assertEquals(marker, updated.title)
            assertTrue(updated.pinned)
            assertEquals(7, updated.sortOrder)
            assertEquals(1, BookmarksClient.groupItemCounts()[group.id])
            assertTrue(checkNotNull(BookmarksClient.updateGroup(group.id) { collapsed = true }).collapsed)
            BookmarksClient.deleteGroup(group.id)
            assertNull(BookmarksClient.getGroupById(group.id))
            assertEquals("", checkNotNull(BookmarksClient.getById(bookmark.id)).groupId)
            assertEquals(0, BookmarksClient.deleteBookmarks(emptySet()))
            assertEquals(1, BookmarksClient.deleteBookmarks(ids))
            assertNull(BookmarksClient.getById(bookmark.id))
        } finally {
            BookmarksClient.deleteBookmarks(ids)
            BookmarksClient.deleteGroup(group.id)
        }
    }
}
