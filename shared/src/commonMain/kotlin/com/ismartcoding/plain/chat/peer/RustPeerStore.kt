package com.ismartcoding.plain.chat.peer

import com.ismartcoding.plain.chat.callChatStore
import com.ismartcoding.plain.chat.models.PeerAddress
import com.ismartcoding.plain.chat.models.PeerRecord
import com.ismartcoding.plain.enums.DeviceType
import com.ismartcoding.plain.enums.PeerStatus
import kotlin.time.Instant
import kotlinx.serialization.json.*

object RustPeerStore {
    suspend fun getAll(): List<PeerRecord> = list(emptyList())
    suspend fun getAllPaired(): List<PeerRecord> = list(listOf(PeerStatus.PAIRED))
    suspend fun getAllWithPublicKey(): List<PeerRecord> = list(listOf(PeerStatus.PAIRED, PeerStatus.CHANNEL))
    private suspend fun list(statuses: List<PeerStatus>): List<PeerRecord> = callChatStore("peers") { put("statuses", JsonArray(statuses.map { JsonPrimitive(it.name) })) }.jsonArray.map(::decode)
    suspend fun getById(id: String): PeerRecord? = callChatStore("peer") { put("id", id) }.takeUnless { it is JsonNull }?.let(::decode)
    suspend fun getByIds(ids: List<String>): List<PeerRecord> = callChatStore("peersByIds") { put("ids", JsonArray(ids.map(::JsonPrimitive))) }.jsonArray.map(::decode)
    suspend fun insert(vararg item: PeerRecord) { save(item.toList(), "INSERT") }
    suspend fun update(vararg item: PeerRecord) { save(item.toList(), "UPDATE") }
    suspend fun upsert(vararg item: PeerRecord) { save(item.toList(), "UPSERT") }
    private suspend fun save(items: List<PeerRecord>, mode: String) { callChatStore("savePeers") { put("mode", mode); put("items", JsonArray(items.map(::encode))) } }
    suspend fun patch(before: PeerRecord, after: PeerRecord): PeerRecord? = callChatStore("patchPeer") { put("before", encode(before)); put("after", encode(after)) }.takeUnless { it is JsonNull }?.let(::decode)
    suspend fun remove(id: String): Boolean = callChatStore("removePeer") { put("id", id) }.jsonPrimitive.boolean
    suspend fun unpair(id: String): Boolean = callChatStore("unpairPeer") { put("id", id) }.jsonPrimitive.boolean
    suspend fun discovered(id: String, ips: List<String>, port: Int, name: String, type: DeviceType): PeerRecord? = callChatStore("discoverPeer") {
        put("id", id); put("ips", JsonArray(ips.map(::JsonPrimitive))); put("port", port); put("name", name); put("device_type", type.name)
    }.takeUnless { it is JsonNull }?.let(::decode)
    suspend fun delete(id: String) { deleteByIds(listOf(id)) }
    suspend fun deleteByIds(ids: List<String>) { callChatStore("deletePeers") { put("ids", JsonArray(ids.map(::JsonPrimitive))) } }
    suspend fun address(peer: PeerRecord): PeerAddress = decodeAddress(callChatStore("peerAddress") {
        put("id", peer.id); put("expected", encode(peer))
    })
    suspend fun fileUrl(peer: PeerRecord, fileId: String): String = callChatStore("peerFileUrl") {
        put("id", peer.id); put("expected", encode(peer)); put("file_id", fileId)
    }.jsonPrimitive.content
    private fun decodeAddress(value: JsonElement): PeerAddress = com.ismartcoding.plain.lib.JsonHelper.jsonDecodeFromElement(value)
    internal fun encode(row: PeerRecord): JsonObject = com.ismartcoding.plain.lib.JsonHelper.jsonEncodeToElement(row).jsonObject
    internal fun decode(value: JsonElement): PeerRecord = com.ismartcoding.plain.lib.JsonHelper.jsonDecodeFromElement(value)
}
