package com.ismartcoding.plain.features.feeds

import com.ismartcoding.plain.models.FeedEntry

suspend fun FeedEntry.fetchContentAsync() {
    val refreshed = FeedEntriesClient.syncContent(id)
    content = refreshed.content
    image = refreshed.image
    updatedAt = refreshed.updatedAt
}
