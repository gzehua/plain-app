package com.ismartcoding.plain.features.bookmarks

import com.ismartcoding.plain.rust.graphql.RustGraphqlApi

import com.ismartcoding.plain.models.Bookmark
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.lib.logcat.LogCat
import com.ismartcoding.plain.rust.graphql.gql
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.*

object BookmarksClient {
    private const val FIELDS = "id url title faviconPath groupId pinned clickCount lastClickedAt sortOrder createdAt updatedAt"

    suspend fun fetchAndUpdateSingle(bookmarkId: String): Bookmark? = try {
        RustGraphqlApi.mutate("fetchBookmarkMetadata(id: ${gql(bookmarkId)}) { $FIELDS }")
            .getValue("fetchBookmarkMetadata").takeUnless { it is JsonNull }?.bookmark()
    } catch (cancelled: CancellationException) { throw cancelled }
    catch (e: Exception) { LogCat.e("Bookmark metadata: ${e.message}"); null }

    private fun JsonElement.bookmark(): Bookmark = JsonHelper.jsonDecodeFromElement<Bookmark>(this)
}
