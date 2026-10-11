package com.ismartcoding.plain.chat.channel

import com.ismartcoding.plain.chat.callChatStore
import com.ismartcoding.plain.chat.models.ChatChannelRecord
import com.ismartcoding.plain.enums.ChatChannelStatus
import com.ismartcoding.plain.graphql.models.ChatChannelMember
import com.ismartcoding.plain.lib.JsonHelper
import kotlin.time.Instant
import kotlinx.serialization.json.*

object RustChannelStore {
    suspend fun create(name: String): ChatChannelRecord = decode(callChatStore("createChannel") { put("actor", com.ismartcoding.plain.TempData.clientId); put("name", name) })
    suspend fun action(id: String, kind: String, name: String? = null, peer: String? = null): ChatChannelRecord = decode(callChatStore("channelAction") {
        put("actor", com.ismartcoding.plain.TempData.clientId); put("id", id); put("operation", buildJsonObject {
            put("kind", kind); name?.let { put("name", it) }; peer?.let { put("peer", it) }
        })
    })
    suspend fun getAll(): List<ChatChannelRecord> = callChatStore("channels").jsonArray.map(::decode)
    suspend fun getById(id: String): ChatChannelRecord? = callChatStore("channel") { put("id", id) }.takeUnless { it is JsonNull }?.let(::decode)
    suspend fun insert(vararg item: ChatChannelRecord) { save(item.toList(), "INSERT") }
    suspend fun update(vararg item: ChatChannelRecord) { save(item.toList(), "UPDATE") }
    private suspend fun save(items: List<ChatChannelRecord>, mode: String) { callChatStore("saveChannels") { put("mode", mode); put("items", JsonArray(items.map(::encode))) } }
    suspend fun patch(before: ChatChannelRecord, after: ChatChannelRecord): ChatChannelRecord = decode(callChatStore("patchChannel") { put("before", encode(before)); put("after", encode(after)) })
    suspend fun remove(id: String): ChatChannelRecord? = callChatStore("removeChannel") { put("id", id) }.takeUnless { it is JsonNull }?.let(::decode)
    suspend fun delete(id: String) { deleteByIds(listOf(id)) }
    suspend fun deleteByIds(ids: List<String>) { callChatStore("deleteChannels") { put("ids", JsonArray(ids.map(::JsonPrimitive))) } }
    fun encode(row: ChatChannelRecord): JsonObject = JsonHelper.jsonEncodeToElement(row).jsonObject
    internal fun decode(value: JsonElement): ChatChannelRecord = JsonHelper.jsonDecodeFromElement(value)
}
