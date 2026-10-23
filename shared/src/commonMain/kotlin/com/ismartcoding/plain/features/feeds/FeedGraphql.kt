package com.ismartcoding.plain.features.feeds

import kotlinx.serialization.json.*
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.models.Feed
import com.ismartcoding.plain.models.FeedEntry

internal fun JsonElement.feed(): Feed = JsonHelper.jsonDecodeFromElement<Feed>(this)
internal fun JsonElement.entry(): FeedEntry = JsonHelper.jsonDecodeFromElement<FeedEntry>(this)
