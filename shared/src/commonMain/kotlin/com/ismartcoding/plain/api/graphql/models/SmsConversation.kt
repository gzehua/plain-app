package com.ismartcoding.plain.api.graphql.models

import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
data class SmsConversation(
    var id: String,
    var address: String,
    var snippet: String,
    var lastMessageAt: Instant,
    var messageCount: Int,
    var read: Boolean,
    var addresses: List<String>,
)
