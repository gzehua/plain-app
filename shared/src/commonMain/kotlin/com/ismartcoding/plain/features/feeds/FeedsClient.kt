package com.ismartcoding.plain.features.feeds


import com.ismartcoding.plain.db.*
import com.ismartcoding.plain.api.graphql.models.Feed
import com.ismartcoding.plain.api.graphql.models.FeedEntryCount
import com.ismartcoding.plain.rust.bridge.*
import com.ismartcoding.plain.features.feeds.*
import com.ismartcoding.plain.ui.feeds.*
import com.ismartcoding.plain.features.files.*
import com.ismartcoding.plain.ui.files.*
import com.ismartcoding.plain.features.notes.*
import com.ismartcoding.plain.ui.notes.*
import com.ismartcoding.plain.features.tags.*
import com.ismartcoding.plain.ui.tags.*
import com.ismartcoding.plain.rust.request.*
import com.ismartcoding.plain.rust.event.*
import com.ismartcoding.plain.rust.host.*
import com.ismartcoding.plain.rust.http.*
import com.ismartcoding.plain.rust.request.*
import kotlinx.serialization.json.*

object FeedsClient {
    suspend fun getAll(): List<Feed> = RustContentApi.query("feeds { $FEED_FIELDS }").getValue("feeds").jsonArray.map { it.feed() }
    suspend fun getFeedCounts(): List<FeedEntryCount> = RustContentApi.query("feedEntryCounts { id count }").getValue("feedEntryCounts").let { com.ismartcoding.plain.lib.JsonHelper.jsonDecodeFromElement<List<FeedEntryCount>>(it) }
    suspend fun getById(id: String): Feed? = RustContentApi.query("feed(id: ${gql(id)}) { $FEED_FIELDS }")["feed"]?.takeUnless { it is JsonNull }?.feed()
    suspend fun getByUrl(url: String): Feed? = getAll().firstOrNull { it.url == url }
    suspend fun addAsync(updateItem: Feed.() -> Unit): String {
        val item = Feed().apply(updateItem)
        val created = RustContentApi.mutate("createFeed(url: ${gql(item.url)}, fetchContent: ${item.fetchContent}) { $FEED_FIELDS }").getValue("createFeed").feed()
        if (created.name != item.name && item.name.isNotEmpty()) updateAsync(created.id) { name = item.name }
        return created.id
    }
    suspend fun updateAsync(id: String, updateItem: Feed.() -> Unit): String {
        val current = getById(id) ?: error("Feed $id not found")
        val oldUrl = current.url
        current.updateItem()
        if (oldUrl != current.url) RustContentApi.mutate("updateFeedUrl(id: ${gql(id)}, url: ${gql(current.url)}) { id }")
        RustContentApi.mutate("updateFeed(id: ${gql(id)}, name: ${gql(current.name)}, fetchContent: ${current.fetchContent}) { id }")
        return id
    }
    suspend fun deleteAsync(ids: Set<String>) { ids.forEach { RustContentApi.mutate("deleteFeed(id: ${gql(it)})") } }
    suspend fun sync(feedId: String) { RustContentApi.mutate("syncFeeds(id: ${if (feedId.isEmpty()) "null" else gql(feedId)})") }
    suspend fun syncStates(): List<com.ismartcoding.plain.api.graphql.models.FeedSyncState> =
        com.ismartcoding.plain.lib.JsonHelper.jsonDecodeFromElement(RustContentApi.query("feedSyncStates { feedId taskId revision status error }").getValue("feedSyncStates"))
}
