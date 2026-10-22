package com.ismartcoding.plain.features.feeds

import com.ismartcoding.plain.rust.graphql.RustGraphqlApi

import com.ismartcoding.plain.models.FeedEntry
import com.ismartcoding.plain.rust.graphql.gql
import com.ismartcoding.plain.rust.graphql.selectionQuery
import kotlinx.serialization.json.*

object FeedEntriesClient {
    suspend fun count(query: String): Int = RustGraphqlApi.query("feedEntryCount(query: ${gql(query)})").getValue("feedEntryCount").jsonPrimitive.int
    suspend fun search(query: String, limit: Int, offset: Int): List<FeedEntry> = RustGraphqlApi.query("feedEntries(query: ${gql(query)}, limit: $limit, offset: $offset) { $ENTRY_FIELDS }").getValue("feedEntries").jsonArray.map { it.entry() }
    suspend fun getAsync(id: String): FeedEntry? = RustGraphqlApi.query("feedEntry(id: ${gql(id)}) { $ENTRY_FIELDS }")["feedEntry"]?.takeUnless { it is JsonNull }?.entry()
    suspend fun syncContent(id: String): FeedEntry = RustGraphqlApi.mutate("syncFeedEntryContent(id: ${gql(id)}) { $ENTRY_FIELDS }").getValue("syncFeedEntryContent").entry()
    suspend fun markReadAsync(ids: Set<String>, read: Boolean = true) { if (ids.isNotEmpty()) RustGraphqlApi.mutate("markFeedEntriesRead(query: ${gql(selectionQuery(ids))}, read: $read) { affectedCount }") }
    suspend fun deleteAsync(ids: Set<String>) { if (ids.isNotEmpty()) RustGraphqlApi.mutate("deleteFeedEntries(query: ${gql(selectionQuery(ids))}) { affectedCount }") }
    suspend fun deleteQuery(query: String) { RustGraphqlApi.mutate("deleteFeedEntries(query: ${gql(query)}) { affectedCount }") }
    suspend fun deleteAllAsync() = deleteQuery("all:true")
}
