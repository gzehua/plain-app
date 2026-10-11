package com.ismartcoding.plain.chat.models

import com.ismartcoding.plain.graphql.models.ChatChannelMember
import com.ismartcoding.plain.lib.JsonHelper
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object ChannelMembersSerializer : KSerializer<List<ChatChannelMember>> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("ChannelMembers", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: List<ChatChannelMember>) = encoder.encodeString(JsonHelper.jsonEncode(value))
    override fun deserialize(decoder: Decoder): List<ChatChannelMember> = JsonHelper.jsonDecode(decoder.decodeString())
}
