package com.ismartcoding.plain.models

import kotlinx.serialization.Serializable

@Serializable
data class ChatDownloadSnapshot(
    val runtimeId: String,
    val revision: Long,
    val tasks: List<ChatDownloadRow>,
)
