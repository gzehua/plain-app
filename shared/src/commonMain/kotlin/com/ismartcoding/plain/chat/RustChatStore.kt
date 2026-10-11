package com.ismartcoding.plain.chat

import com.ismartcoding.plain.db.*
import com.ismartcoding.plain.enums.ChatStatus
import com.ismartcoding.plain.rust.PlainRust
import com.ismartcoding.plain.rust.content.gql
import kotlinx.serialization.json.*
import kotlin.time.Instant

object RustChatStore {
    internal const val FIELDS = "id fromId toId channelId content status statusData createdAt updatedAt"
    internal fun decodeGraphql(value: JsonElement): DChat = value.jsonObject.let { row ->
        fun string(key: String) = row.getValue(key).jsonPrimitive.content
        DChat(id = string("id"), fromId = string("fromId"), toId = string("toId"), channelId = row["channelId"]?.jsonPrimitive?.contentOrNull.orEmpty(), status = ChatStatus.valueOf(string("status")), statusData = string("statusData"), content = DChat.parseContent(string("content")), createdAt = Instant.parse(string("createdAt")), updatedAt = Instant.parse(string("updatedAt")))
    }

    suspend fun getAll(): List<DChat> = page()
    suspend fun getByPeerId(id: String): List<DChat> = page(peer = id)
    suspend fun getByChannelId(id: String): List<DChat> = page(channel = id)
    suspend fun getByPeerIdPage(id: String, limit: Int, offset: Int): List<DChat> = page(peer = id, limit = limit, offset = offset, descending = true)
    suspend fun getByChannelIdPage(id: String, limit: Int, offset: Int): List<DChat> = page(channel = id, limit = limit, offset = offset, descending = true)
    suspend fun getByPeerIdPageText(id: String, text: String, limit: Int, offset: Int): List<DChat> = page(peer = id, text = text, limit = limit, offset = offset, descending = true)
    suspend fun getByChannelIdPageText(id: String, text: String, limit: Int, offset: Int): List<DChat> = page(channel = id, text = text, limit = limit, offset = offset, descending = true)
    suspend fun getLatestConversations(): Map<String, DChat> =
        PlainRust.mainQuery("chatConversations { id chat { $FIELDS } }").getValue("chatConversations").jsonArray.associate {
            it.jsonObject.getValue("id").jsonPrimitive.content to decodeGraphql(it.jsonObject.getValue("chat"))
        }
    suspend fun search(text: String, limit: Int, offset: Int): List<DChat> = page(text = text, limit = limit, offset = offset, descending = true)
    suspend fun count(text: String): Int = PlainRust.mainQuery("chatCount(query: ${gql("text:${term(text)}")})").getValue("chatCount").jsonPrimitive.int
    suspend fun getIds(query: String): Set<String> = PlainRust.mainQuery("chatIds(query: ${gql(query)})").getValue("chatIds").jsonArray.map { it.jsonPrimitive.content }.toSet()
    suspend fun page(peer: String? = null, channel: String? = null, text: String = "", offset: Int = 0, limit: Int? = null, descending: Boolean = false, latest: Boolean = false): List<DChat> {
        val query = listOfNotNull(peer?.let { "peer:${term(it)}" }, channel?.let { "channel:${term(it)}" }, "text:${term(text)}", if (descending) "sort:desc" else null, if (latest) "latest:true" else null).joinToString(" ")
        return PlainRust.mainQuery("chatRecords(query: ${gql(query)}, offset: $offset, limit: ${limit ?: Int.MAX_VALUE}) { $FIELDS }").getValue("chatRecords").jsonArray.map(::decodeGraphql)
    }
    suspend fun getById(id: String): DChat? = PlainRust.mainQuery("chatItem(id: ${gql(id)}) { $FIELDS }").getValue("chatItem").takeUnless { it is JsonNull }?.let(::decodeGraphql)
    private fun term(value: String): String = "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\""
}
