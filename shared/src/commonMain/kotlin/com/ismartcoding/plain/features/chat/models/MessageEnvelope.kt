package com.ismartcoding.plain.features.chat.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
internal data class MessageEnvelope(
    val type: MessageType,
    val value: JsonElement,
)
