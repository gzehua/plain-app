package com.ismartcoding.plain.graphql.models

import com.ismartcoding.plain.chat.models.MessageContent
import com.ismartcoding.plain.chat.models.MessageFiles
import com.ismartcoding.plain.chat.models.MessageImages
import com.ismartcoding.plain.chat.models.MessageShare
import com.ismartcoding.plain.chat.models.MessageStatusData
import com.ismartcoding.plain.chat.models.MessageText
import com.ismartcoding.plain.chat.models.MessageType
import com.ismartcoding.plain.db.*
import com.ismartcoding.plain.enums.ChatStatus
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
                val obj = JsonHelper.json.parseToJsonElement(content).jsonObject
                val typeStr = obj["type"]?.jsonPrimitive?.content ?: ""
                val message = MessageContent(MessageType.entries.firstOrNull { it.name == typeStr } ?: MessageType.TEXT)
                val valueJson = obj["value"]?.takeIf { it !is JsonNull }?.toString() ?: ""
                when (message.type) {
                    MessageType.TEXT -> message.value = JsonHelper.json.decodeFromString<MessageText>(valueJson)
                    MessageType.IMAGES -> message.value = JsonHelper.json.decodeFromString<MessageImages>(valueJson)
                    MessageType.FILES -> message.value = JsonHelper.json.decodeFromString<MessageFiles>(valueJson)
                    MessageType.SHARE -> message.value = JsonHelper.json.decodeFromString<MessageShare>(valueJson)
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
