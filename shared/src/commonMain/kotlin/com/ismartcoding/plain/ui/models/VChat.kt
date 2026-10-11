package com.ismartcoding.plain.ui.models

import com.ismartcoding.plain.chat.models.MessageStatusData
import com.ismartcoding.plain.chat.models.MessageType
import com.ismartcoding.plain.db.IData
import com.ismartcoding.plain.enums.ChatStatus
import com.ismartcoding.plain.graphql.models.ChatItem
import kotlin.time.Instant

data class VChat(
    override var id: String,
    val fromId: String,
    val createdAt: Instant,
    val type: MessageType,
    val status: ChatStatus,
    val statusData: MessageStatusData? = null,
    val value: Any? = null,
) : IData {
    companion object {
        fun from(data: ChatItem, fromName: String = ""): VChat {
            return VChat(data.id, data.fromId, data.createdAt, data.messageContent.type, data.status, data.parseStatusData(), data.messageContent.value)
        }
    }
}
