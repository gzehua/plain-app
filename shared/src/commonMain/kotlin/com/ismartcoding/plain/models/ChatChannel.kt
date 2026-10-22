package com.ismartcoding.plain.models

import com.ismartcoding.plain.features.chat.ChatChannelStatus
import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
data class ChatChannel(
    val actions: ChatChannelActions,
    var id: String,
    var ownerId: String,
    var name: String,
    var members: List<ChatChannelMember>,
    var version: Long,
    var status: ChatChannelStatus,
    var createdAt: Instant,
    var updatedAt: Instant,
)
