package com.ismartcoding.plain.features.feeds

import com.ismartcoding.plain.models.Feed
import com.ismartcoding.plain.models.FeedEntryCount
import com.ismartcoding.plain.models.FeedPreview
import com.ismartcoding.plain.models.FeedSyncState
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.rust.request.RustApi
import com.ismartcoding.plain.rust.request.gql
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

internal object FeedsClient : FeedsApi(
    query = { RustApi.query(it) },
    mutate = { RustApi.mutate(it) },
)

internal open class FeedsApi(
    private val query: suspend (String) -> JsonObject,
    private val mutate: suspend (String) -> JsonObject,
) {
    suspend fun getAll(withCount: Boolean = false): List<Feed> {
        val data = query("feeds { $FEED_FIELDS }" + if (withCount) " feedEntryCounts { id count }" else "")
        val counts = if (withCount) JsonHelper.jsonDecodeFromElement<List<FeedEntryCount>>(data.getValue("feedEntryCounts")).associate { it.id to it.count } else emptyMap()
        return data.getValue("feeds").jsonArray.map { it.feed().apply { entryCount = counts[id] ?: 0 } }
    }
    suspend fun getById(id: String): Feed? = query("feed(id: ${gql(id)}) { $FEED_FIELDS }")["feed"]?.takeUnless { it is JsonNull }?.feed()
    suspend fun preview(url: String): FeedPreview = JsonHelper.jsonDecodeFromElement(query("feedPreview(url: ${gql(url)}) { title }").getValue("feedPreview"))
    suspend fun create(url: String, name: String, fetchContent: Boolean = false): Feed = mutate("createFeed(url: ${gql(url)}, name: ${gql(name)}, fetchContent: $fetchContent) { $FEED_FIELDS }").getValue("createFeed").feed()
    suspend fun update(id: String, name: String? = null, url: String? = null, fetchContent: Boolean? = null): Feed {
        val arguments = listOfNotNull("id: ${gql(id)}", name?.let { "name: ${gql(it)}" }, url?.let { "url: ${gql(it)}" }, fetchContent?.let { "fetchContent: $it" })
        return mutate("updateFeed(${arguments.joinToString(", ")}) { $FEED_FIELDS }").getValue("updateFeed").feed()
    }
    suspend fun deleteAsync(ids: Set<String>) { ids.forEach { mutate("deleteFeed(id: ${gql(it)})") } }
    suspend fun importAsync(content: String) { mutate("importFeeds(content: ${gql(content)})") }
    suspend fun exportAsync(): String = mutate("exportFeeds").getValue("exportFeeds").jsonPrimitive.content
    suspend fun sync(feedId: String) { mutate("syncFeeds(id: ${if (feedId.isEmpty()) "null" else gql(feedId)})") }
    suspend fun syncStates(): List<FeedSyncState> = JsonHelper.jsonDecodeFromElement(query("feedSyncStates { feedId taskId revision status error }").getValue("feedSyncStates"))
}
