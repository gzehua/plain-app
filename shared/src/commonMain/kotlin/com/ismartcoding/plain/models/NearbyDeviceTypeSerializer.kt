package com.ismartcoding.plain.models

import com.ismartcoding.plain.platform.system.DeviceType
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object NearbyDeviceTypeSerializer : KSerializer<DeviceType> {
    override val descriptor = PrimitiveSerialDescriptor("NearbyDeviceType", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: DeviceType) = encoder.encodeString(value.name)

    override fun deserialize(decoder: Decoder): DeviceType {
        val name = decoder.decodeString()
        return DeviceType.entries.firstOrNull { it.name == name } ?: DeviceType.OTHER
    }
}
