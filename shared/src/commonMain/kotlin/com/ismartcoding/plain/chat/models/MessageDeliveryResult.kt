package com.ismartcoding.plain.chat.models

import kotlinx.serialization.Serializable

@Serializable
data class MessageDeliveryResult(
    val peerId: String,
    val peerName: String,
    val error: String? = null,
)
