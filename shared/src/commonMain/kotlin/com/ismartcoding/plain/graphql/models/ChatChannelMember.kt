package com.ismartcoding.plain.graphql.models

import com.ismartcoding.plain.enums.ChannelMemberStatus
import kotlinx.serialization.Serializable

@Serializable
data class ChatChannelMember(
    @kotlinx.serialization.json.JsonNames("id") var peerId: String,
    var status: ChannelMemberStatus = ChannelMemberStatus.JOINED,
) {
    fun isJoined(): Boolean = status == ChannelMemberStatus.JOINED
    fun isPending(): Boolean = status == ChannelMemberStatus.PENDING
}
