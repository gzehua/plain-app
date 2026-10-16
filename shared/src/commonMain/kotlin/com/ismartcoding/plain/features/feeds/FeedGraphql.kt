package com.ismartcoding.plain.features.feeds

import kotlinx.serialization.json.*
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.api.graphql.models.Feed
import com.ismartcoding.plain.api.graphql.models.FeedEntry

internal const val FEED_FIELDS = "id name url logo fetchContent lastSyncAt lastError { code detail } createdAt updatedAt"
internal const val ENTRY_FIELDS = "id title url image description author content feedId rawId publishedAt read createdAt updatedAt"
internal fun JsonElement.feed(): Feed = JsonHelper.jsonDecodeFromElement<Feed>(this)
internal fun JsonElement.entry(): FeedEntry = JsonHelper.jsonDecodeFromElement<FeedEntry>(this)
