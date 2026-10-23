package com.ismartcoding.plain.models

import com.ismartcoding.plain.features.chat.ChannelMemberStatus
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@Serializable
data class ChatChannelMember(
    @JsonNames("id") var peerId: String,
    var status: ChannelMemberStatus = ChannelMemberStatus.JOINED,
) {
    fun isJoined(): Boolean = status == ChannelMemberStatus.JOINED
    fun isPending(): Boolean = status == ChannelMemberStatus.PENDING
}
