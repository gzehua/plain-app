package com.ismartcoding.plain.features.chat.models

import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
class MessageShare(
    val shareId: String,
    val urlToken: String,
    val peerInfo: SharePeerInfo,
    val name: String,
    val itemCount: Int = 0,
    val totalSize: Long = 0,
    /** Null = never expires. */
    val expiresAt: Instant? = null,
)
