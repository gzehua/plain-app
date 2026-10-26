package com.ismartcoding.plain.features.chat

import com.ismartcoding.plain.graphql.Query
import com.ismartcoding.plain.rust.graphql.RustGraphqlApi
import com.ismartcoding.plain.models.ChatItem
import com.ismartcoding.plain.lib.JsonHelper
import kotlinx.serialization.json.*
import com.ismartcoding.plain.models.ChatConversation

object RustChatStore {
    internal fun decodeGraphql(value: JsonElement): ChatItem = JsonHelper.jsonDecodeFromElement(value)

    suspend fun getAll(): List<ChatItem> = page()
    suspend fun getByPeerId(id: String): List<ChatItem> = page(peer = id)
    suspend fun getByChannelId(id: String): List<ChatItem> = page(channel = id)
    suspend fun getLatestConversations(): Map<String, ChatItem> =
        JsonHelper.jsonDecodeFromElement<List<ChatConversation>>(
            RustGraphqlApi.graphql(Query.chatConversations()).getValue("chatConversations"),
        ).associate { it.id to it.chat }
    suspend fun search(text: String, limit: Int, offset: Int): List<ChatItem> = page(text = text, limit = limit, offset = offset, descending = true)
    suspend fun count(text: String): Int = RustGraphqlApi.graphql(Query.chatCount("text:${term(text)}")).getValue("chatCount").jsonPrimitive.int
    suspend fun page(peer: String? = null, channel: String? = null, text: String = "", offset: Int = 0, limit: Int? = null, descending: Boolean = false, latest: Boolean = false): List<ChatItem> {
        val query = listOfNotNull(peer?.let { "peer:${term(it)}" }, channel?.let { "channel:${term(it)}" }, "text:${term(text)}", if (descending) "sort:desc" else null, if (latest) "latest:true" else null).joinToString(" ")
        return RustGraphqlApi.graphql(Query.chatRecords(query, offset, limit ?: Int.MAX_VALUE)).getValue("chatRecords").jsonArray.map(::decodeGraphql)
    }
    suspend fun getById(id: String): ChatItem? = RustGraphqlApi.graphql(Query.chatItem(id)).getValue("chatItem").takeUnless { it is JsonNull }?.let(::decodeGraphql)
    private fun term(value: String): String = "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\""
}
