package com.ismartcoding.plain.features.feeds

import com.ismartcoding.plain.graphql.Mutation
import com.ismartcoding.plain.graphql.Query
import com.ismartcoding.plain.models.Feed
import com.ismartcoding.plain.models.FeedEntryCount
import com.ismartcoding.plain.models.FeedPreview
import com.ismartcoding.plain.models.FeedSyncState
import com.ismartcoding.plain.rust.graphql.GraphqlRequest
import com.ismartcoding.plain.rust.graphql.RustGraphqlApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

internal object FeedsClient : FeedsApi(
    query = { RustGraphqlApi.graphql(it) },
    mutate = { RustGraphqlApi.graphql(it) },
)
internal open class FeedsApi(
    private val query: suspend (GraphqlRequest) -> JsonObject,
    private val mutate: suspend (GraphqlRequest) -> JsonObject,
) {

    suspend fun getAll(withCount: Boolean = false): List<Feed> {
        val request = Query.feeds(withCount)
        val data = if (withCount) {
            RustGraphqlApi.readResponse<FeedsResponse>(request, query)
        } else {
            FeedsResponse(RustGraphqlApi.read(request, query), emptyList())
        }
        val counts = data.feedEntryCounts.associate { it.id to it.count }
        return data.feeds.map { it.apply { entryCount = counts[id] ?: 0 } }
    }

    suspend fun getById(id: String): Feed? =
        RustGraphqlApi.read(Query.feed(id), query)

    suspend fun preview(url: String): FeedPreview =
        RustGraphqlApi.read(Query.feedPreview(url), query)

    suspend fun create(url: String, name: String, fetchContent: Boolean = false): Feed =
        RustGraphqlApi.read(Mutation.createFeed(url, name, fetchContent), mutate)

    suspend fun update(id: String, name: String? = null, url: String? = null, fetchContent: Boolean? = null): Feed {
        return RustGraphqlApi.read(Mutation.updateFeed(id, name, url, fetchContent), mutate)
    }

    suspend fun deleteAsync(ids: Set<String>) {
        ids.forEach { mutate(Mutation.deleteFeed(it)) }
    }

    suspend fun importAsync(content: String) {
        mutate(Mutation.importFeeds(content))
    }

    suspend fun exportAsync(): String = RustGraphqlApi.read(Mutation.exportFeeds(), mutate)

    suspend fun sync(feedId: String) {
        mutate(Mutation.syncFeeds(feedId))
    }

    suspend fun syncStates(): List<FeedSyncState> =
        RustGraphqlApi.read(Query.feedSyncStates(), query)
}

@Serializable
private data class FeedsResponse(
    val feeds: List<Feed>,
    val feedEntryCounts: List<FeedEntryCount>,
)
