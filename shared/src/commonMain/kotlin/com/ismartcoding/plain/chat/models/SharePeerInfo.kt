package com.ismartcoding.plain.chat.models

import kotlinx.serialization.Serializable

@Serializable
class SharePeerInfo(
    val id: String,
    val ip: String,
    val port: Int,
)
