package com.ismartcoding.plain.features.sessions

import com.ismartcoding.plain.platform.system.DeviceType
import kotlinx.serialization.Serializable

@Serializable
data class AuthPeer(
    val deviceName: String,
    val port: Int,
    val deviceType: DeviceType,
    val ips: List<String>,
    val signaturePublicKey: String,
)
