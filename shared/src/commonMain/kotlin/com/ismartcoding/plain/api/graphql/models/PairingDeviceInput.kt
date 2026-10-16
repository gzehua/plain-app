package com.ismartcoding.plain.api.graphql.models

import com.ismartcoding.plain.platform.system.DeviceType
import com.ismartcoding.plain.features.nearby.DiscoveryMethod
import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
data class PairingDeviceInput(
    var id: String,
    var name: String,
    var ips: List<String>,
    var port: Int,
    var deviceType: DeviceType,
    var version: String,
    var platform: String,
    var lastSeen: Instant,
    var discoveryMethods: List<DiscoveryMethod>,
)
