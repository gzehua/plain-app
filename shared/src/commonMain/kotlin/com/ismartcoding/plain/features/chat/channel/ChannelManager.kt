package com.ismartcoding.plain.features.chat.channel

import com.ismartcoding.plain.models.ChatChannel
import com.ismartcoding.plain.features.chat.peer.GraphQLResponse

object ChannelManager {
    suspend fun createChannel(name: String): ChatChannel = createChannelGraphql(name)

    suspend fun renameChannel(channelId: String, newName: String): ChatChannel = updateChannelGraphql(channelId, newName)

    suspend fun deleteChannel(channelId: String) {
        deleteChannelGraphql(channelId)
    }

    suspend fun leaveChannel(channelId: String) {
        leaveChannelGraphql(channelId)
    }

    suspend fun inviteMember(channelId: String, peerId: String): ChatChannel = addMemberGraphql(channelId, peerId)

    suspend fun resendInvite(channelId: String, peerId: String) {
        addMemberGraphql(channelId, peerId)
    }

    suspend fun kickMember(channelId: String, peerId: String): ChatChannel = removeMemberGraphql(channelId, peerId)

    suspend fun acceptInvite(channelId: String): GraphQLResponse = acceptInviteGraphql(channelId)

    suspend fun declineInvite(channelId: String) {
        declineInviteGraphql(channelId)
    }
}
