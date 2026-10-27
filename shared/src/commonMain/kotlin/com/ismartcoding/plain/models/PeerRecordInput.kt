package com.ismartcoding.plain.models

import com.ismartcoding.plain.features.nearby.PeerStatus
import com.ismartcoding.plain.platform.system.DeviceType
import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
internal data class PeerRecordInput(
    val id: String,
    val name: String,
    val ip: String,
    val key: String,
    val publicKey: String,
    val status: PeerStatus,
    val port: Int?,
    val deviceType: DeviceType,
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
            status = peer.status,
            port = peer.port,
            deviceType = peer.deviceType,
            token = peer.token,
            createdAt = peer.createdAt,
            updatedAt = peer.updatedAt,
        )
    }
}
