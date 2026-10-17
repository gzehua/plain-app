package com.ismartcoding.plain.features.chat.peer

import com.ismartcoding.plain.rust.PlainRust
import com.ismartcoding.plain.rust.request.gql
import com.ismartcoding.plain.api.graphql.models.Peer
import com.ismartcoding.plain.api.graphql.models.PeerRecordInput
import com.ismartcoding.plain.lib.JsonHelper
import kotlinx.serialization.json.*

object RustPeerStore {
    private const val FIELDS = "id name ip key publicKey status port deviceType token createdAt updatedAt address { bestIp name baseUrl apiUrl statusWsUrl }"
    suspend fun getAll(): List<Peer> = PlainRust.mainQuery("peers { $FIELDS }").getValue("peers").jsonArray.map(::decode)
    suspend fun getById(id: String): Peer? = getAll().find { it.id == id }
    suspend fun remove(id: String): Boolean = PlainRust.mainMutate("deletePeer(id: ${gql(id)})").getValue("deletePeer").jsonPrimitive.boolean
    suspend fun unpair(id: String): Boolean = PlainRust.mainMutate("unpairPeer(id: ${gql(id)})").getValue("unpairPeer").jsonPrimitive.boolean
    suspend fun fileUrl(peer: Peer, fileId: String): String {
        val expected = JsonHelper.jsonEncodeToElement(PeerRecordInput.from(peer))
        return PlainRust.mainGraphql("query(\$id: ID!, \$expected: PeerRecordInput!, \$fileId: String!) { peerFileUrl(id: \$id, expected: \$expected, fileId: \$fileId) }",mapOf("id" to JsonPrimitive(peer.id),"expected" to expected,"fileId" to JsonPrimitive(fileId))).getValue("peerFileUrl").jsonPrimitive.content
    }
    internal fun decode(value: JsonElement): Peer = JsonHelper.jsonDecodeFromElement(value)
}
