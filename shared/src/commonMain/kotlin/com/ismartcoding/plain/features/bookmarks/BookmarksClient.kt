package com.ismartcoding.plain.features.bookmarks

import com.ismartcoding.plain.api.graphql.models.Bookmark
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.lib.logcat.LogCat
import com.ismartcoding.plain.rust.request.RustContentApi
import com.ismartcoding.plain.rust.request.gql
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.*

object BookmarksClient {
    private const val FIELDS = "id url title faviconPath groupId pinned clickCount lastClickedAt sortOrder createdAt updatedAt"

    suspend fun fetchAndUpdateSingle(bookmarkId: String): Bookmark? = try {
        RustContentApi.mutate("fetchBookmarkMetadata(id: ${gql(bookmarkId)}) { $FIELDS }")
            .getValue("fetchBookmarkMetadata").takeUnless { it is JsonNull }?.bookmark()
    } catch (cancelled: CancellationException) { throw cancelled }
    catch (e: Exception) { LogCat.e("Bookmark metadata: ${e.message}"); null }

    private fun JsonElement.bookmark(): Bookmark = JsonHelper.jsonDecodeFromElement<Bookmark>(this)
}
