package com.ismartcoding.plain.features.nearby

import kotlinx.serialization.Serializable

@Serializable(with = DiscoveryMethodSerializer::class)
enum class DiscoveryMethod {
    LAN,
    DNS_SD,
    BLE,
    QR,
    UNKNOWN,
}
