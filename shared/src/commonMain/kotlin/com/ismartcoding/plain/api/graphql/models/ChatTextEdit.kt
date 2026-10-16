package com.ismartcoding.plain.api.graphql.models

import kotlinx.serialization.Serializable

@Serializable
data class ChatTextEdit(
    var changed: Boolean,
    var chat: ChatItem,
)
