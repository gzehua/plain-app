package com.ismartcoding.plain.models

import com.ismartcoding.plain.features.chat.ChannelMemberStatus
import kotlinx.serialization.Serializable

@Serializable
data class ChatChannelMember(
    @kotlinx.serialization.json.JsonNames("id") var peerId: String,
    var status: ChannelMemberStatus = ChannelMemberStatus.JOINED,
) {
    fun isJoined(): Boolean = status == ChannelMemberStatus.JOINED
    fun isPending(): Boolean = status == ChannelMemberStatus.PENDING
}
