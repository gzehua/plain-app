package com.ismartcoding.plain.graphql.models

import kotlinx.serialization.Serializable

@Serializable
data class PairingTicket(
    val deviceId: String,
    val deviceName: String,
    val deviceIp: String,
    val devicePort: Int,
    val generation: String,
    val delayMs: Long,
)
