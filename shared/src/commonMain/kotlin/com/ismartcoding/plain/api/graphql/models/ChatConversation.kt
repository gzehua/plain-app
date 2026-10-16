package com.ismartcoding.plain.api.graphql.models

import kotlinx.serialization.Serializable

@Serializable
data class ChatConversation(
    var id: String,
    var chat: ChatItem,
)
