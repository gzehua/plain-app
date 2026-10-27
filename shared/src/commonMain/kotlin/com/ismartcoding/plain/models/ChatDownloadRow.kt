package com.ismartcoding.plain.models

import com.ismartcoding.plain.features.chat.models.MessageFile
import com.ismartcoding.plain.features.downloads.DownloadStatus
import kotlinx.serialization.Serializable

@Serializable
data class ChatDownloadRow(
    val file: MessageFile,
    val peer: Peer,
    val messageId: String,
    val status: DownloadStatus,
    val error: String,
    val downloaded: Long,
    val speed: Long,
)
