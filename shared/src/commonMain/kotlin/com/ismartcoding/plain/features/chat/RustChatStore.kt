package com.ismartcoding.plain.features.chat

import com.ismartcoding.plain.rust.graphql.RustGraphqlApi

import com.ismartcoding.plain.models.ChatItem
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.rust.graphql.gql
import kotlinx.serialization.json.*

object RustChatStore {
    internal const val FIELDS = "id fromId toId channelId content status statusData createdAt updatedAt"
    internal fun decodeGraphql(value: JsonElement): ChatItem = JsonHelper.jsonDecodeFromElement(value)

    suspend fun getAll(): List<ChatItem> = page()
    suspend fun getByPeerId(id: String): List<ChatItem> = page(peer = id)
    suspend fun getByChannelId(id: String): List<ChatItem> = page(channel = id)
    suspend fun getLatestConversations(): Map<String, ChatItem> =
        JsonHelper.jsonDecodeFromElement<List<com.ismartcoding.plain.models.ChatConversation>>(
            RustGraphqlApi.query("chatConversations { id chat { $FIELDS } }").getValue("chatConversations"),
        ).associate { it.id to it.chat }
    suspend fun search(text: String, limit: Int, offset: Int): List<ChatItem> = page(text = text, limit = limit, offset = offset, descending = true)
    suspend fun count(text: String): Int = RustGraphqlApi.query("chatCount(query: ${gql("text:${term(text)}")})").getValue("chatCount").jsonPrimitive.int
    suspend fun page(peer: String? = null, channel: String? = null, text: String = "", offset: Int = 0, limit: Int? = null, descending: Boolean = false, latest: Boolean = false): List<ChatItem> {
        val query = listOfNotNull(peer?.let { "peer:${term(it)}" }, channel?.let { "channel:${term(it)}" }, "text:${term(text)}", if (descending) "sort:desc" else null, if (latest) "latest:true" else null).joinToString(" ")
        return RustGraphqlApi.query("chatRecords(query: ${gql(query)}, offset: $offset, limit: ${limit ?: Int.MAX_VALUE}) { $FIELDS }").getValue("chatRecords").jsonArray.map(::decodeGraphql)
    }
    suspend fun getById(id: String): ChatItem? = RustGraphqlApi.query("chatItem(id: ${gql(id)}) { $FIELDS }").getValue("chatItem").takeUnless { it is JsonNull }?.let(::decodeGraphql)
    private fun term(value: String): String = "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\""
}
