package com.ismartcoding.plain.tests


import com.ismartcoding.plain.api.graphql.models.Bookmark
import com.ismartcoding.plain.api.graphql.models.BookmarkGroup
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.rust.request.RustContentApi
import kotlinx.serialization.json.*

internal object TestBookmarksClient {
    private const val FIELDS = "id url title faviconPath groupId pinned clickCount lastClickedAt sortOrder createdAt updatedAt"
    private const val GROUP_FIELDS = "id name collapsed sortOrder itemCount createdAt updatedAt"

    suspend fun getAll(): List<Bookmark> = RustContentApi.query("bookmarks { $FIELDS }")
        .getValue("bookmarks").jsonArray.map { it.bookmark() }
    suspend fun getById(id: String): Bookmark? = getAll().find { it.id == id }
    suspend fun getAllGroups(): List<BookmarkGroup> = groupRows().map { it.group() }
    suspend fun getGroupById(id: String): BookmarkGroup? = getAllGroups().find { it.id == id }
    suspend fun groupItemCounts(): Map<String, Int> = groupRows().associate {
        it.jsonObject.getValue("id").jsonPrimitive.content to it.jsonObject.getValue("itemCount").jsonPrimitive.int
    }
    private suspend fun groupRows(): JsonArray = RustContentApi.query("bookmarkGroups { $GROUP_FIELDS }")
        .getValue("bookmarkGroups").jsonArray

    suspend fun addBookmarks(urls: List<String>, groupId: String = ""): List<Bookmark> =
        RustContentApi.mutate("addBookmarks(urls: ${gqlIds(urls)}, groupId: ${gql(groupId)}) { $FIELDS }")
            .getValue("addBookmarks").jsonArray.map { it.bookmark() }

    suspend fun updateBookmark(id: String, block: Bookmark.() -> Unit): Bookmark? {
        val row = getById(id)?.apply(block) ?: return null
        val input = "{url: ${gql(row.url)}, title: ${gql(row.title)}, groupId: ${gql(row.groupId)}, pinned: ${row.pinned}, sortOrder: ${row.sortOrder}}"
        return RustContentApi.mutate("updateBookmark(id: ${gql(id)}, input: $input) { $FIELDS }")
            .getValue("updateBookmark").bookmark()
    }

    suspend fun deleteBookmarks(ids: Set<String>): Int {
        if (ids.isEmpty()) return 0
        return RustContentApi.mutate("deleteBookmarks(ids: ${gqlIds(ids)}) { affectedCount }")
            .getValue("deleteBookmarks").jsonObject.getValue("affectedCount").jsonPrimitive.int
    }
    suspend fun recordClick(id: String) { RustContentApi.mutate("recordBookmarkClick(id: ${gql(id)})") }
    suspend fun createGroup(name: String): BookmarkGroup =
        RustContentApi.mutate("createBookmarkGroup(name: ${gql(name)}) { $GROUP_FIELDS }").getValue("createBookmarkGroup").group()
    suspend fun updateGroup(id: String, block: BookmarkGroup.() -> Unit): BookmarkGroup? {
        val row = getGroupById(id)?.apply(block) ?: return null
        return RustContentApi.mutate("updateBookmarkGroup(id: ${gql(id)}, name: ${gql(row.name)}, collapsed: ${row.collapsed}, sortOrder: ${row.sortOrder}) { $GROUP_FIELDS }")
            .getValue("updateBookmarkGroup").group()
    }
    suspend fun deleteGroup(id: String) { RustContentApi.mutate("deleteBookmarkGroup(id: ${gql(id)})") }
    private fun JsonElement.bookmark(): Bookmark = JsonHelper.jsonDecodeFromElement<Bookmark>(this)
    private fun JsonElement.group(): BookmarkGroup = JsonHelper.jsonDecodeFromElement<BookmarkGroup>(this)
}

private fun gql(value: String): String = JsonPrimitive(value).toString()
private fun gqlIds(values: Collection<String>): String = values.joinToString(",", "[", "]") { gql(it) }
