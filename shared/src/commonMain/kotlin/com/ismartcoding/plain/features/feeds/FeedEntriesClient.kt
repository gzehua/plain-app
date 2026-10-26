package com.ismartcoding.plain.features.feeds

import com.ismartcoding.plain.graphql.Mutation
import com.ismartcoding.plain.graphql.Query
import com.ismartcoding.plain.rust.graphql.RustGraphqlApi
import com.ismartcoding.plain.models.FeedEntry
import com.ismartcoding.plain.rust.graphql.selectionQuery
import kotlinx.serialization.json.*

object FeedEntriesClient {
    suspend fun count(query: String): Int = RustGraphqlApi.graphql(Query.feedEntryCount(query)).getValue("feedEntryCount").jsonPrimitive.int
    suspend fun search(query: String, limit: Int, offset: Int): List<FeedEntry> = RustGraphqlApi.graphql(Query.feedEntries(query, limit, offset)).getValue("feedEntries").jsonArray.map { it.entry() }
    suspend fun getAsync(id: String): FeedEntry? = RustGraphqlApi.graphql(Query.feedEntry(id))["feedEntry"]?.takeUnless { it is JsonNull }?.entry()
    suspend fun syncContent(id: String): FeedEntry = RustGraphqlApi.graphql(Mutation.syncFeedEntryContent(id)).getValue("syncFeedEntryContent").entry()
    suspend fun markReadAsync(ids: Set<String>, read: Boolean = true) { if (ids.isNotEmpty()) RustGraphqlApi.graphql(Mutation.markFeedEntriesRead(selectionQuery(ids), read)) }
    suspend fun deleteAsync(ids: Set<String>) { if (ids.isNotEmpty()) RustGraphqlApi.graphql(Mutation.deleteFeedEntries(selectionQuery(ids))) }
    suspend fun deleteQuery(query: String) { RustGraphqlApi.graphql(Mutation.deleteFeedEntries(query)) }
    suspend fun deleteAllAsync() = deleteQuery("all:true")
}
