package com.ismartcoding.plain.features.feeds

import com.ismartcoding.plain.models.FeedEntry
import com.ismartcoding.plain.rust.PlainRust
import com.ismartcoding.plain.rust.request.gql
import com.ismartcoding.plain.rust.request.selectionQuery
import kotlinx.serialization.json.*

object FeedEntriesClient {
    suspend fun count(query: String): Int = PlainRust.mainQuery("feedEntryCount(query: ${gql(query)})").getValue("feedEntryCount").jsonPrimitive.int
    suspend fun search(query: String, limit: Int, offset: Int): List<FeedEntry> = PlainRust.mainQuery("feedEntries(query: ${gql(query)}, limit: $limit, offset: $offset) { $ENTRY_FIELDS }").getValue("feedEntries").jsonArray.map { it.entry() }
    suspend fun getAsync(id: String): FeedEntry? = PlainRust.mainQuery("feedEntry(id: ${gql(id)}) { $ENTRY_FIELDS }")["feedEntry"]?.takeUnless { it is JsonNull }?.entry()
    suspend fun syncContent(id: String): FeedEntry = PlainRust.mainMutate("syncFeedEntryContent(id: ${gql(id)}) { $ENTRY_FIELDS }").getValue("syncFeedEntryContent").entry()
    suspend fun markReadAsync(ids: Set<String>, read: Boolean = true) { if (ids.isNotEmpty()) PlainRust.mainMutate("markFeedEntriesRead(query: ${gql(selectionQuery(ids))}, read: $read) { affectedCount }") }
    suspend fun deleteAsync(ids: Set<String>) { if (ids.isNotEmpty()) PlainRust.mainMutate("deleteFeedEntries(query: ${gql(selectionQuery(ids))}) { affectedCount }") }
    suspend fun deleteQuery(query: String) { PlainRust.mainMutate("deleteFeedEntries(query: ${gql(query)}) { affectedCount }") }
    suspend fun deleteAllAsync() = deleteQuery("all:true")
}
