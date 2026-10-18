package com.ismartcoding.plain.api.graphql.models

import com.ismartcoding.plain.features.chat.models.MessageContent
import com.ismartcoding.plain.features.chat.models.MessageFiles
import com.ismartcoding.plain.features.chat.models.MessageImages
import com.ismartcoding.plain.features.chat.models.MessageShare
import com.ismartcoding.plain.features.chat.models.MessageStatusData
import com.ismartcoding.plain.features.chat.models.MessageText
import com.ismartcoding.plain.features.chat.models.MessageType
import com.ismartcoding.plain.db.*
import com.ismartcoding.plain.features.chat.ChatStatus
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.lib.TimeHelper
import com.ismartcoding.plain.lib.generateId
import kotlin.time.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

@Serializable
data class ChatItem(
    @kotlinx.serialization.Required var fromId: String = "",
    @kotlinx.serialization.Required var toId: String = "",
    var channelId: String? = null,
    @kotlinx.serialization.Required var id: String = generateId(),
    var content: String,
    @kotlinx.serialization.Required var createdAt: Instant = TimeHelper.now(),
    @kotlinx.serialization.Required var updatedAt: Instant = TimeHelper.now(),
    @kotlinx.serialization.Required var status: ChatStatus = ChatStatus.PENDING,
    @kotlinx.serialization.Required var statusData: String = "",
) {
    val messageContent: MessageContent get() = parseContent(content)
    fun parseStatusData(): MessageStatusData? = MessageStatusData.fromJson(statusData)

    companion object {
        fun parseContent(content: String): MessageContent {
            return try {
                val envelope = JsonHelper.jsonDecode<MessagePayload>(content)
                val typeStr = envelope.type
                val message = MessageContent(MessageType.entries.firstOrNull { it.name == typeStr } ?: MessageType.TEXT)
                val value = envelope.value
                when (message.type) {
                    MessageType.TEXT -> message.value = JsonHelper.jsonDecodeFromElement<MessageText>(value)
                    MessageType.IMAGES -> message.value = JsonHelper.jsonDecodeFromElement<MessageImages>(value)
                    MessageType.FILES -> message.value = JsonHelper.jsonDecodeFromElement<MessageFiles>(value)
                    MessageType.SHARE -> message.value = JsonHelper.jsonDecodeFromElement<MessageShare>(value)
                }
                message
            } catch (_: Exception) {
                // Legacy or unknown payload shapes: keep the type consistent
                // with the fallback value and render the raw content as text.
                MessageContent(MessageType.TEXT, MessageText(content))
            }
        }
    }
}

@Serializable
private data class MessagePayload(val type: String = "", val value: JsonElement = JsonNull)
