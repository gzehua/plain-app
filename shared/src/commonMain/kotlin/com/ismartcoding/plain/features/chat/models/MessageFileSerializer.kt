package com.ismartcoding.plain.features.chat.models

import com.ismartcoding.plain.lib.generateId
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonNames
import kotlinx.serialization.EncodeDefault

@Serializable
private data class MessageFileSurrogate(
    val id: String = generateId(),
    val uri: String,
    val size: Long,
    @JsonNames( "duration")
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    val legacyDurationSec: Long? = null,
    val durationMs: Long? = null,
    val width: Int = 0,
    val height: Int = 0,
    val summary: String = "",
    val fileName: String = "",
)

object MessageFileSerializer : KSerializer<MessageFile> {
    private val surrogateSerializer = MessageFileSurrogate.serializer()
    override val descriptor: SerialDescriptor = surrogateSerializer.descriptor

    override fun serialize(encoder: Encoder, value: MessageFile) {
        encoder.encodeSerializableValue(
            surrogateSerializer,
            MessageFileSurrogate(
                id = value.id,
                uri = value.uri,
                size = value.size,
                durationMs = value.durationMs,
                width = value.width,
                height = value.height,
                summary = value.summary,
                fileName = value.fileName,
            ),
        )
    }

    override fun deserialize(decoder: Decoder): MessageFile {
        val raw = decoder.decodeSerializableValue(surrogateSerializer)
        return MessageFile(
            id = raw.id,
            uri = raw.uri,
            size = raw.size,
            durationMs = raw.durationMs ?: (raw.legacyDurationSec ?: 0L) * 1000,
            width = raw.width,
            height = raw.height,
            summary = raw.summary,
            fileName = raw.fileName,
        )
    }
}
