package com.ismartcoding.plain.graphql.models

import com.ismartcoding.plain.enums.DeviceType
import com.ismartcoding.plain.enums.PeerStatus
import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Peer(
    var id: String,
    var name: String,
    var ip: String,
    var status: PeerStatus,
    var port: Int?,
    var deviceType: DeviceType,
    var createdAt: Instant,
    var updatedAt: Instant,
    var online: Boolean,
)
