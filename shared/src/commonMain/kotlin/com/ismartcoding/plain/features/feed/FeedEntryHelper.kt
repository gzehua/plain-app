package com.ismartcoding.plain.features.feed

import com.ismartcoding.plain.graphql.models.FeedEntry
import com.ismartcoding.plain.rust.bridge.*
import com.ismartcoding.plain.rust.content.*
import com.ismartcoding.plain.rust.event.*
import com.ismartcoding.plain.rust.host.*
import com.ismartcoding.plain.rust.http.*
import com.ismartcoding.plain.rust.request.*
import kotlinx.serialization.json.*

object FeedEntryHelper {
    suspend fun count(query: String): Int = RustContentApi.query("feedEntryCount(query: ${gql(query)})").getValue("feedEntryCount").jsonPrimitive.int
    suspend fun search(query: String, limit: Int, offset: Int): List<FeedEntry> = RustContentApi.query("feedEntries(query: ${gql(query)}, limit: $limit, offset: $offset) { $ENTRY_FIELDS }").getValue("feedEntries").jsonArray.map { it.entry() }
    suspend fun getIdsAsync(query: String): Set<String> = RustContentApi.query("feedEntries(query: ${gql(query)}, limit: ${Int.MAX_VALUE}, offset: 0) { id }").getValue("feedEntries").jsonArray.map { it.jsonObject.string("id") }.toSet()
    suspend fun getAsync(id: String): FeedEntry? = RustContentApi.query("feedEntry(id: ${gql(id)}) { $ENTRY_FIELDS }")["feedEntry"]?.takeUnless { it is JsonNull }?.entry()
    suspend fun syncContent(id: String): FeedEntry = RustContentApi.mutate("syncFeedEntryContent(id: ${gql(id)}) { $ENTRY_FIELDS }").getValue("syncFeedEntryContent").entry()
    suspend fun markReadAsync(ids: Set<String>, read: Boolean = true) { if (ids.isNotEmpty()) RustContentApi.mutate("markFeedEntriesRead(query: ${gql(selectionQuery(ids))}, read: $read) { affectedCount }") }
    suspend fun deleteAsync(ids: Set<String>) { if (ids.isNotEmpty()) RustContentApi.mutate("deleteFeedEntries(query: ${gql(selectionQuery(ids))}) { affectedCount }") }
    suspend fun deleteAllAsync() { RustContentApi.mutate("deleteFeedEntries(query: \"all:true\") { affectedCount }") }
    suspend fun deleteByFeedIdsAsync(ids: Set<String>) { ids.forEach { RustContentApi.mutate("deleteFeedEntries(query: ${gql("feed_id:$it")}) { affectedCount }") } }
}
