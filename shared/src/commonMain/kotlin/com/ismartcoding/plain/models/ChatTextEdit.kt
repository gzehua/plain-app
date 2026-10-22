package com.ismartcoding.plain.models

import kotlinx.serialization.Serializable

@Serializable
data class ChatTextEdit(
    var changed: Boolean,
    var chat: ChatItem,
)
