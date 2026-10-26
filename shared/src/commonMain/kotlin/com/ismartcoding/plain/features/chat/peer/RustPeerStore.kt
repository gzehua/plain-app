package com.ismartcoding.plain.features.chat.peer

import com.ismartcoding.plain.graphql.Mutation
import com.ismartcoding.plain.graphql.Query
import com.ismartcoding.plain.rust.graphql.RustGraphqlApi
import com.ismartcoding.plain.models.Peer
import com.ismartcoding.plain.models.PeerRecordInput
import com.ismartcoding.plain.lib.JsonHelper
import kotlinx.serialization.json.*

object RustPeerStore {
    suspend fun getAll(): List<Peer> = RustGraphqlApi.graphql(Query.peers()).getValue("peers").jsonArray.map(::decode)
    suspend fun getById(id: String): Peer? = getAll().find { it.id == id }
    suspend fun remove(id: String): Boolean = RustGraphqlApi.graphql(Mutation.deletePeer(id)).getValue("deletePeer").jsonPrimitive.boolean
    suspend fun unpair(id: String): Boolean = RustGraphqlApi.graphql(Mutation.unpairPeer(id)).getValue("unpairPeer").jsonPrimitive.boolean
    suspend fun fileUrl(peer: Peer, fileId: String): String {
        return RustGraphqlApi.graphql(Query.peerFileUrl(peer.id, PeerRecordInput.from(peer), fileId)).getValue("peerFileUrl").jsonPrimitive.content
    }
    internal fun decode(value: JsonElement): Peer = JsonHelper.jsonDecodeFromElement(value)
}
