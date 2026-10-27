package com.ismartcoding.plain.features.chat

import com.ismartcoding.plain.graphql.Query
import com.ismartcoding.plain.models.ChatConversation
import com.ismartcoding.plain.models.ChatItem
import com.ismartcoding.plain.rust.graphql.RustGraphqlApi

object RustChatStore {
    suspend fun getAll(): List<ChatItem> = page()

    suspend fun getByPeerId(id: String): List<ChatItem> = page(peer = id)

    suspend fun getByChannelId(id: String): List<ChatItem> = page(channel = id)

    suspend fun getLatestConversations(): Map<String, ChatItem> =
        RustGraphqlApi.read<List<ChatConversation>>(Query.chatConversations()).associate { it.id to it.chat }

    suspend fun search(
        text: String,
        limit: Int,
        offset: Int,
    ): List<ChatItem> = page(text = text, limit = limit, offset = offset, descending = true)

    suspend fun count(text: String): Int = RustGraphqlApi.read(Query.chatCount("text:${term(text)}"))

    suspend fun page(
        peer: String? = null,
        channel: String? = null,
        text: String = "",
        offset: Int = 0,
        limit: Int? = null,
        descending: Boolean = false,
        latest: Boolean = false,
    ): List<ChatItem> {
        val query = listOfNotNull(
            peer?.let { "peer:${term(it)}" },
            channel?.let { "channel:${term(it)}" },
            "text:${term(text)}",
            if (descending) "sort:desc" else null,
            if (latest) "latest:true" else null,
        ).joinToString(" ")
        return RustGraphqlApi.read(Query.chatRecords(query, offset, limit ?: Int.MAX_VALUE))
    }

    suspend fun getById(id: String): ChatItem? = RustGraphqlApi.read(Query.chatItem(id))

    private fun term(value: String): String = "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\""
}
