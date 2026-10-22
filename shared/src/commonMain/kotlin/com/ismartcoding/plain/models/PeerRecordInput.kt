package com.ismartcoding.plain.models

import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
internal data class PeerRecordInput(
    val id: String,
    val name: String,
    val ip: String,
    val key: String,
    val publicKey: String,
    val status: String,
    val port: Int?,
    val deviceType: String,
    val token: String,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    companion object {
        fun from(peer: Peer) = PeerRecordInput(
            id = peer.id,
            name = peer.name,
            ip = peer.ip,
            key = peer.key,
            publicKey = peer.publicKey,
            status = peer.status.name,
            port = peer.port,
            deviceType = peer.deviceType.name,
            token = peer.token,
            createdAt = peer.createdAt,
            updatedAt = peer.updatedAt,
        )
    }
}
