package com.ismartcoding.plain.features.chat.models

import com.ismartcoding.plain.models.ChatItem
import com.ismartcoding.plain.lib.JsonHelper
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

object MessageContentSerializer : KSerializer<MessageContent> {
    override val descriptor: SerialDescriptor = MessageEnvelope.serializer().descriptor

    override fun serialize(encoder: Encoder, value: MessageContent) {
        val payload = value.value?.let {
            when (value.type) {
                MessageType.TEXT -> JsonHelper.jsonEncodeToElement(it as MessageText)
                MessageType.IMAGES -> JsonHelper.jsonEncodeToElement(it as MessageImages)
                MessageType.FILES -> JsonHelper.jsonEncodeToElement(it as MessageFiles)
                MessageType.SHARE -> JsonHelper.jsonEncodeToElement(it as MessageShare)
            }
        } ?: JsonObject(emptyMap())
        encoder.encodeSerializableValue(MessageEnvelope.serializer(), MessageEnvelope(value.type.name, payload))
    }

    override fun deserialize(decoder: Decoder): MessageContent =
        ChatItem.parseContent(decoder.decodeSerializableValue(JsonElement.serializer()).toString())
}
