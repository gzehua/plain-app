package com.ismartcoding.plain.features.bookmarks

import com.ismartcoding.plain.graphql.Mutation
import com.ismartcoding.plain.lib.logcat.LogCat
import com.ismartcoding.plain.models.Bookmark
import com.ismartcoding.plain.rust.graphql.RustGraphqlApi
import kotlinx.coroutines.CancellationException

object BookmarksClient {
    suspend fun fetchAndUpdateSingle(bookmarkId: String): Bookmark? = try {
        RustGraphqlApi.read(Mutation.fetchBookmarkMetadata(bookmarkId))
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (e: Exception) {
        LogCat.e("Bookmark metadata: ${e.message}")
        null
    }
}
