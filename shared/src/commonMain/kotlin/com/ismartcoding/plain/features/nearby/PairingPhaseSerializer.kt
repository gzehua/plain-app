package com.ismartcoding.plain.features.nearby

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object PairingPhaseSerializer : KSerializer<PairingPhase> {
    override val descriptor = PrimitiveSerialDescriptor("PairingPhase", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: PairingPhase) = encoder.encodeString(value.name)

    override fun deserialize(decoder: Decoder): PairingPhase {
        val name = decoder.decodeString()
        return PairingPhase.entries.firstOrNull { it.name == name } ?: PairingPhase.UNKNOWN
    }
}
