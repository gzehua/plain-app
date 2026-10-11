package com.ismartcoding.plain.graphql.models

import com.ismartcoding.plain.enums.ChatChannelStatus
import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
data class ChatChannel(
    var id: String,
    var ownerId: String,
    var name: String,
    var members: List<ChatChannelMember>,
    var version: Long,
    var status: ChatChannelStatus,
    var createdAt: Instant,
    var updatedAt: Instant,
)
