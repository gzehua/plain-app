package com.ismartcoding.plain.api.graphql.models

import com.ismartcoding.plain.platform.system.DeviceType
import com.ismartcoding.plain.features.nearby.PeerStatus
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
