package com.ismartcoding.plain.features.feeds

import com.ismartcoding.plain.graphql.Mutation
import com.ismartcoding.plain.graphql.Query
import com.ismartcoding.plain.models.FeedEntry
import com.ismartcoding.plain.rust.graphql.RustGraphqlApi
import com.ismartcoding.plain.rust.graphql.selectionQuery

object FeedEntriesClient {
    suspend fun count(query: String): Int =
        RustGraphqlApi.execute(Query.feedEntryCount(query))

    suspend fun search(query: String, limit: Int, offset: Int): List<FeedEntry> =
        RustGraphqlApi.execute(Query.feedEntries(query, limit, offset))

    suspend fun getAsync(id: String): FeedEntry? =
        RustGraphqlApi.execute(Query.feedEntry(id))

    suspend fun syncContent(id: String): FeedEntry =
        RustGraphqlApi.execute(Mutation.syncFeedEntryContent(id))

    suspend fun markReadAsync(ids: Set<String>, read: Boolean = true) {
        if (ids.isNotEmpty()) RustGraphqlApi.execute(Mutation.markFeedEntriesRead(selectionQuery(ids), read))
    }

    suspend fun deleteAsync(ids: Set<String>) {
        if (ids.isNotEmpty()) RustGraphqlApi.execute(Mutation.deleteFeedEntries(selectionQuery(ids)))
    }

    suspend fun deleteQuery(query: String) {
        RustGraphqlApi.execute(Mutation.deleteFeedEntries(query))
    }

    suspend fun deleteAllAsync() = deleteQuery("all:true")
}
