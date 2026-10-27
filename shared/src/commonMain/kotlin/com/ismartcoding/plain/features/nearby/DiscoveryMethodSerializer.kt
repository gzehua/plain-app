package com.ismartcoding.plain.features.nearby

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object DiscoveryMethodSerializer : KSerializer<DiscoveryMethod> {
    override val descriptor = PrimitiveSerialDescriptor("DiscoveryMethod", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: DiscoveryMethod) = encoder.encodeString(value.name)

    override fun deserialize(decoder: Decoder): DiscoveryMethod {
        val name = decoder.decodeString()
        return DiscoveryMethod.entries.firstOrNull { it.name == name } ?: DiscoveryMethod.UNKNOWN
    }
}
