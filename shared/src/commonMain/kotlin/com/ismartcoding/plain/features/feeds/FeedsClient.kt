package com.ismartcoding.plain.features.feeds

import com.ismartcoding.plain.rust.graphql.GraphqlRequest
import com.ismartcoding.plain.graphql.Mutation
import com.ismartcoding.plain.graphql.Query
import com.ismartcoding.plain.rust.graphql.RustGraphqlApi
import com.ismartcoding.plain.models.Feed
import com.ismartcoding.plain.models.FeedEntryCount
import com.ismartcoding.plain.models.FeedPreview
import com.ismartcoding.plain.models.FeedSyncState
import com.ismartcoding.plain.lib.JsonHelper
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

internal object FeedsClient : FeedsApi(
    query = { RustGraphqlApi.graphql(it) },
    mutate = { RustGraphqlApi.graphql(it) },
)

internal open class FeedsApi(
    private val query: suspend (GraphqlRequest) -> JsonObject,
    private val mutate: suspend (GraphqlRequest) -> JsonObject,
) {
    suspend fun getAll(withCount: Boolean = false): List<Feed> {
        val data = query(Query.feeds(withCount))
        val counts = if (withCount) JsonHelper.jsonDecodeFromElement<List<FeedEntryCount>>(data.getValue("feedEntryCounts")).associate { it.id to it.count } else emptyMap()
        return data.getValue("feeds").jsonArray.map { it.feed().apply { entryCount = counts[id] ?: 0 } }
    }
    suspend fun getById(id: String): Feed? = query(Query.feed(id))["feed"]?.takeUnless { it is JsonNull }?.feed()
    suspend fun preview(url: String): FeedPreview = JsonHelper.jsonDecodeFromElement(query(Query.feedPreview(url)).getValue("feedPreview"))
    suspend fun create(url: String, name: String, fetchContent: Boolean = false): Feed = mutate(Mutation.createFeed(url, name, fetchContent)).getValue("createFeed").feed()
    suspend fun update(id: String, name: String? = null, url: String? = null, fetchContent: Boolean? = null): Feed {
        return mutate(Mutation.updateFeed(id, name, url, fetchContent)).getValue("updateFeed").feed()
    }
    suspend fun deleteAsync(ids: Set<String>) { ids.forEach { mutate(Mutation.deleteFeed(it)) } }
    suspend fun importAsync(content: String) { mutate(Mutation.importFeeds(content)) }
    suspend fun exportAsync(): String = mutate(Mutation.exportFeeds()).getValue("exportFeeds").jsonPrimitive.content
    suspend fun sync(feedId: String) { mutate(Mutation.syncFeeds(feedId)) }
    suspend fun syncStates(): List<FeedSyncState> = JsonHelper.jsonDecodeFromElement(query(Query.feedSyncStates()).getValue("feedSyncStates"))
}
