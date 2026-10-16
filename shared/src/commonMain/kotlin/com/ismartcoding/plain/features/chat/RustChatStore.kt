package com.ismartcoding.plain.features.chat

import com.ismartcoding.plain.db.*
import com.ismartcoding.plain.features.chat.ChatStatus
import com.ismartcoding.plain.api.graphql.models.ChatItem
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.rust.PlainRust
import com.ismartcoding.plain.rust.request.gql
import kotlin.time.Instant
import kotlinx.serialization.json.*

object RustChatStore {
    internal const val FIELDS = "id fromId toId channelId content status statusData createdAt updatedAt"
    internal fun decodeGraphql(value: JsonElement): ChatItem = JsonHelper.jsonDecodeFromElement(value)

    suspend fun getAll(): List<ChatItem> = page()
    suspend fun getByPeerId(id: String): List<ChatItem> = page(peer = id)
    suspend fun getByChannelId(id: String): List<ChatItem> = page(channel = id)
    suspend fun getByPeerIdPage(id: String, limit: Int, offset: Int): List<ChatItem> = page(peer = id, limit = limit, offset = offset, descending = true)
    suspend fun getByChannelIdPage(id: String, limit: Int, offset: Int): List<ChatItem> = page(channel = id, limit = limit, offset = offset, descending = true)
    suspend fun getByPeerIdPageText(id: String, text: String, limit: Int, offset: Int): List<ChatItem> = page(peer = id, text = text, limit = limit, offset = offset, descending = true)
    suspend fun getByChannelIdPageText(id: String, text: String, limit: Int, offset: Int): List<ChatItem> = page(channel = id, text = text, limit = limit, offset = offset, descending = true)
    suspend fun getLatestConversations(): Map<String, ChatItem> =
        JsonHelper.jsonDecodeFromElement<List<com.ismartcoding.plain.api.graphql.models.ChatConversation>>(
            PlainRust.mainQuery("chatConversations { id chat { $FIELDS } }").getValue("chatConversations"),
        ).associate { it.id to it.chat }
    suspend fun search(text: String, limit: Int, offset: Int): List<ChatItem> = page(text = text, limit = limit, offset = offset, descending = true)
    suspend fun count(text: String): Int = PlainRust.mainQuery("chatCount(query: ${gql("text:${term(text)}")})").getValue("chatCount").jsonPrimitive.int
    suspend fun getIds(query: String): Set<String> = PlainRust.mainQuery("chatIds(query: ${gql(query)})").getValue("chatIds").jsonArray.map { it.jsonPrimitive.content }.toSet()
    suspend fun page(peer: String? = null, channel: String? = null, text: String = "", offset: Int = 0, limit: Int? = null, descending: Boolean = false, latest: Boolean = false): List<ChatItem> {
        val query = listOfNotNull(peer?.let { "peer:${term(it)}" }, channel?.let { "channel:${term(it)}" }, "text:${term(text)}", if (descending) "sort:desc" else null, if (latest) "latest:true" else null).joinToString(" ")
        return PlainRust.mainQuery("chatRecords(query: ${gql(query)}, offset: $offset, limit: ${limit ?: Int.MAX_VALUE}) { $FIELDS }").getValue("chatRecords").jsonArray.map(::decodeGraphql)
    }
    suspend fun getById(id: String): ChatItem? = PlainRust.mainQuery("chatItem(id: ${gql(id)}) { $FIELDS }").getValue("chatItem").takeUnless { it is JsonNull }?.let(::decodeGraphql)
    private fun term(value: String): String = "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\""
}
