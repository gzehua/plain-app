package com.ismartcoding.plain.chat

import kotlinx.serialization.Serializable

@Serializable
internal data class ChatAttachment(
    val fileId: String,
    val name: String,
    val width: Int,
    val height: Int,
    val durationMs: Long,
)
