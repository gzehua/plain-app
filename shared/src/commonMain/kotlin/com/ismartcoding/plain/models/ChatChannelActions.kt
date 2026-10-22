package com.ismartcoding.plain.models

import kotlinx.serialization.Serializable

@Serializable
data class ChatChannelActions(
    val isOwner: Boolean,
    val canRename: Boolean,
    val canManageMembers: Boolean,
    val canLeave: Boolean,
    val canDelete: Boolean,
)
