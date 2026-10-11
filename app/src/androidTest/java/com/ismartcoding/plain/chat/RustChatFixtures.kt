package com.ismartcoding.plain.chat

import com.ismartcoding.plain.chat.models.ChatItemDataUpdate
import com.ismartcoding.plain.chat.models.MessageContent
import com.ismartcoding.plain.chat.models.MessageDeliveryResult
import com.ismartcoding.plain.chat.models.toJSONString
import com.ismartcoding.plain.db.*
import com.ismartcoding.plain.enums.ChatStatus
import com.ismartcoding.plain.graphql.models.ChatItem
import com.ismartcoding.plain.rust.request.RustContentApi
import kotlin.time.Instant
import kotlinx.serialization.json.*

// Low-level fixtures simulate incoming wire data and seed storage without sending.
object RustChatFixtures {
    suspend fun create(toId: String, channelId: String, content: MessageContent): ChatItem = decode(callChatStore("createChat") {
        put("to_id", toId); put("channel_id", channelId); put("content", content.toJSONString())
    })
    suspend fun receive(fromId: String, channelId: String, content: String, signature: String, timestamp: Long): ReceivedChat? {
        val result = callChatStore("receiveChat") {
            put("from_id", fromId); put("channel_id", channelId); put("content", content); put("signature", signature); put("timestamp", timestamp)
        }.takeUnless { it is JsonNull }?.jsonObject ?: return null
        return ReceivedChat(decode(result.getValue("chat")), checkNotNull(com.ismartcoding.plain.chat.peer.RustPeerStore.getById(result.getValue("peer").jsonObject.getValue("id").jsonPrimitive.content)), result.getValue("channel").takeUnless { it is JsonNull }?.let { com.ismartcoding.plain.chat.channel.RustChannelStore.getById(it.jsonObject.getValue("id").jsonPrimitive.content) })
    }
    suspend fun delivery(id: String, results: List<MessageDeliveryResult>?, retry: Boolean = false): ChatItem? = callChatStore("chatDelivery") {
        put("id", id); put("retry", retry)
        put("results", results?.let { JsonArray(it.map { result -> buildJsonObject {
            put("peerId", result.peerId); put("peerName", result.peerName); put("error", result.error?.let(::JsonPrimitive) ?: JsonNull)
        } }) } ?: JsonNull)
    }.takeUnless { it is JsonNull }?.let(::decode)
    suspend fun insert(vararg item: ChatItem) { save(item.toList(), "INSERT") }
    suspend fun update(vararg item: ChatItem) { save(item.toList(), "UPDATE") }
    private suspend fun save(items: List<ChatItem>, mode: String) { callChatStore("saveChats") { put("mode", mode); put("items", JsonArray(items.map(::encode))) } }
    suspend fun updateStatus(id: String, status: ChatStatus) { status(id, status, null) }
    suspend fun updateStatusAndData(id: String, status: ChatStatus, data: String) { status(id, status, data) }
    private suspend fun status(id: String, status: ChatStatus, data: String?) { check(callChatStore("chatStatus") { put("id", id); put("status", status.name); put("data", data?.let(::JsonPrimitive) ?: JsonNull) }.jsonPrimitive.boolean) { "Chat unavailable" } }
    suspend fun updateData(item: ChatItemDataUpdate) { check(callChatStore("chatContent") { put("id", item.id); put("content", item.content.toJSONString()) }.jsonPrimitive.boolean) { "Chat unavailable" } }
    suspend fun delete(id: String) { deleteByIds(listOf(id)) }
    suspend fun deleteByIds(ids: List<String>) { callChatStore("deleteChats") { put("ids", JsonArray(ids.map(::JsonPrimitive))) } }
    suspend fun deleteByPeerId(id: String) { callChatStore("deletePeerChats") { put("id", id) } }
    suspend fun deleteByChannelId(id: String) { callChatStore("deleteChannelChats") { put("id", id) } }
    private fun encode(row: ChatItem): JsonObject = buildJsonObject {
        put("id", row.id); put("from_id", row.fromId); put("to_id", row.toId); put("channel_id", row.channelId.orEmpty()); put("content", row.content)
        put("status", row.status.name); put("status_data", row.statusData); put("created_at", row.createdAt.toString()); put("updated_at", row.updatedAt.toString())
    }
    internal fun decode(value: JsonElement): ChatItem = value.jsonObject.let { row ->
        fun string(key: String) = row.getValue(key).jsonPrimitive.content
        ChatItem(id = string("id"), fromId = string("from_id"), toId = string("to_id"), channelId = string("channel_id"), status = ChatStatus.valueOf(string("status")), statusData = string("status_data"), content = string("content"), createdAt = Instant.parse(string("created_at")), updatedAt = Instant.parse(string("updated_at")))
    }

    private suspend fun callChatStore(action: String, fields: JsonObjectBuilder.() -> Unit = {}): JsonElement =
        RustContentApi.postJsonOrThrow("chat/store", buildJsonObject { put("action", action); fields() }, longRunning = true).getValue("result")
}
