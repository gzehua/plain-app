package com.ismartcoding.plain.platform

import com.ismartcoding.plain.features.feed.FeedEntryHelper
import com.ismartcoding.plain.graphql.models.FeedEntry
import com.ismartcoding.plain.rust.http.ApiResult
import kotlinx.coroutines.CancellationException

suspend fun FeedEntry.fetchContentAsync(): ApiResult = try {
    val refreshed = FeedEntryHelper.syncContent(id)
    content = refreshed.content
    image = refreshed.image
    updatedAt = refreshed.updatedAt
    ApiResult(null, success = true)
} catch (cancelled: CancellationException) { throw cancelled }
catch (error: Exception) { ApiResult(null, error) }
