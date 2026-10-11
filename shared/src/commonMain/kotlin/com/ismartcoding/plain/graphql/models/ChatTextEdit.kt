package com.ismartcoding.plain.graphql.models

import kotlinx.serialization.Serializable

@Serializable
data class ChatTextEdit(
    var changed: Boolean,
    var chat: ChatItem,
)
