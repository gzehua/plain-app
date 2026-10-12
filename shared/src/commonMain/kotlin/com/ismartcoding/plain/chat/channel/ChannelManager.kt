package com.ismartcoding.plain.chat.channel

import com.ismartcoding.plain.chat.ChatManager
import com.ismartcoding.plain.chat.models.ChatChannelRecord
import com.ismartcoding.plain.chat.peer.GraphQLResponse

object ChannelManager {
    private suspend fun afterMutation() {
        ChannelCacher.load()
        ChatManager.refreshLatestChats()
    }

    suspend fun createChannel(name: String): ChatChannelRecord {
        val record = createChannelGraphql(name)
        afterMutation()
        return record
    }

    suspend fun renameChannel(channelId: String, newName: String): ChatChannelRecord {
        val record = updateChannelGraphql(channelId, newName)
        afterMutation()
        return record
    }

    suspend fun deleteChannel(channelId: String) {
        deleteChannelGraphql(channelId)
        afterMutation()
    }

    suspend fun leaveChannel(channelId: String) {
        leaveChannelGraphql(channelId)
        afterMutation()
    }

    suspend fun inviteMember(channelId: String, peerId: String): ChatChannelRecord {
        val record = addMemberGraphql(channelId, peerId)
        afterMutation()
        return record
    }

    suspend fun resendInvite(channelId: String, peerId: String) {
        addMemberGraphql(channelId, peerId)
        afterMutation()
    }

    suspend fun kickMember(channelId: String, peerId: String): ChatChannelRecord {
        val record = removeMemberGraphql(channelId, peerId)
        afterMutation()
        return record
    }

    suspend fun acceptInvite(channelId: String): GraphQLResponse = acceptInviteGraphql(channelId).also { afterMutation() }

    suspend fun declineInvite(channelId: String) {
        declineInviteGraphql(channelId)
        afterMutation()
    }
}