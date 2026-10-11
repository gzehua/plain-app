package com.ismartcoding.plain.graphql.models

import com.ismartcoding.plain.enums.DeviceType
import com.ismartcoding.plain.enums.DiscoveryMethod
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
