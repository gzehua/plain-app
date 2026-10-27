package com.ismartcoding.plain.platform.system

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object DataTypeSerializer : KSerializer<DataType> {
    override val descriptor = PrimitiveSerialDescriptor("DataType", PrimitiveKind.INT)

    override fun serialize(encoder: Encoder, value: DataType) = encoder.encodeInt(value.value)

    override fun deserialize(decoder: Decoder): DataType {
        val value = decoder.decodeInt()
        return DataType.entries.firstOrNull { it.value == value }
            ?: throw SerializationException("Unknown data type: $value")
    }
}
