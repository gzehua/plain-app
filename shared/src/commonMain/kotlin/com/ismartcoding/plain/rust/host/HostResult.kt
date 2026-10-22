package com.ismartcoding.plain.rust.host

import com.ismartcoding.plain.lib.JsonHelper
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.serializer

@Serializable(with = HostResultSerializer::class)
internal fun interface HostResult {
    fun serialize(encoder: Encoder)

    fun encodeReply(id: Long): String = JsonHelper.jsonEncode(RustHostReply(id, this))
}

internal inline fun <reified T> hostResult(value: T): HostResult =
    HostResult { it.encodeSerializableValue(serializer<T>(), value) }

internal fun hostResult(): HostResult = hostResult<Unit?>(null)

internal object HostResultSerializer : KSerializer<HostResult> {
    override val descriptor = JsonElement.serializer().descriptor
    override fun serialize(encoder: Encoder, value: HostResult) = value.serialize(encoder)
    override fun deserialize(decoder: Decoder): HostResult = throw SerializationException("Host results are output only")
}
