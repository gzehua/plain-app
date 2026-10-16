package com.ismartcoding.plain.features.feeds

import com.ismartcoding.plain.features.feeds.FeedEntriesClient
import com.ismartcoding.plain.api.graphql.models.FeedEntry
import com.ismartcoding.plain.rust.http.ApiResult
import kotlinx.coroutines.CancellationException

suspend fun FeedEntry.fetchContentAsync(): ApiResult = try {
    val refreshed = FeedEntriesClient.syncContent(id)
    content = refreshed.content
    image = refreshed.image
    updatedAt = refreshed.updatedAt
    ApiResult(null, success = true)
} catch (cancelled: CancellationException) { throw cancelled }
catch (error: Exception) { ApiResult(null, error) }
