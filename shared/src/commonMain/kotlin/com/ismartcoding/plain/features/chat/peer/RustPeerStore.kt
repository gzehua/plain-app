package com.ismartcoding.plain.features.chat.peer

import com.ismartcoding.plain.rust.PlainRust
import com.ismartcoding.plain.rust.request.gql
import com.ismartcoding.plain.rust.request.gqlIds
import com.ismartcoding.plain.api.graphql.models.PeerRecord
import com.ismartcoding.plain.api.graphql.models.PeerRecordInput
import com.ismartcoding.plain.lib.JsonHelper
import kotlinx.serialization.json.*

object RustPeerStore {
    private const val FIELDS = "id name ip key publicKey status port deviceType token createdAt updatedAt address { bestIp name baseUrl apiUrl statusWsUrl }"
    suspend fun getAll(): List<PeerRecord> = PlainRust.mainQuery("peerRecords { $FIELDS }").getValue("peerRecords").jsonArray.map(::decode)
    suspend fun getById(id: String): PeerRecord? = PlainRust.mainQuery("peerRecord(id: ${gql(id)}) { $FIELDS }").getValue("peerRecord").takeUnless { it is JsonNull }?.let(::decode)
    suspend fun getByIds(ids: List<String>): List<PeerRecord> = PlainRust.mainQuery("peerRecords(ids: ${gqlIds(ids)}) { $FIELDS }").getValue("peerRecords").jsonArray.map(::decode)
    suspend fun remove(id: String): Boolean = PlainRust.mainMutate("removePeerRecord(id: ${gql(id)})").getValue("removePeerRecord").jsonPrimitive.boolean
    suspend fun unpair(id: String): Boolean = PlainRust.mainMutate("unpairPeerRecord(id: ${gql(id)})").getValue("unpairPeerRecord").jsonPrimitive.boolean
    suspend fun fileUrl(peer: PeerRecord, fileId: String): String {
        val expected = JsonHelper.jsonEncodeToElement(PeerRecordInput.from(peer))
        return PlainRust.mainGraphql("query(\$id: ID!, \$expected: PeerRecordInput!, \$fileId: String!) { peerFileUrl(id: \$id, expected: \$expected, fileId: \$fileId) }",mapOf("id" to JsonPrimitive(peer.id),"expected" to expected,"fileId" to JsonPrimitive(fileId))).getValue("peerFileUrl").jsonPrimitive.content
    }
    internal fun decode(value: JsonElement): PeerRecord = JsonHelper.jsonDecodeFromElement(value)
}
