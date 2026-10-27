package com.ismartcoding.plain.features.chat.channel

import com.ismartcoding.plain.features.chat.peer.GraphQLResponse
import com.ismartcoding.plain.graphql.Mutation
import com.ismartcoding.plain.graphql.Query
import com.ismartcoding.plain.lib.withIO
import com.ismartcoding.plain.models.ChatChannel
import com.ismartcoding.plain.rust.graphql.RustGraphqlApi

internal suspend fun createChannelGraphql(name: String): ChatChannel = withIO {
    RustGraphqlApi.execute(Mutation.createChatChannel(name))
}
internal suspend fun updateChannelGraphql(id: String, name: String): ChatChannel = withIO {
    RustGraphqlApi.execute(Mutation.updateChatChannel(id, name))
}
internal suspend fun deleteChannelGraphql(id: String): Boolean = withIO {
    RustGraphqlApi.execute(Mutation.deleteChatChannel(id))
}
internal suspend fun leaveChannelGraphql(id: String): Boolean = withIO {
    RustGraphqlApi.execute(Mutation.leaveChatChannel(id))
}
internal suspend fun addMemberGraphql(id: String, peerId: String): ChatChannel = withIO {
    RustGraphqlApi.execute(Mutation.addChatChannelMember(id, peerId))
}
internal suspend fun removeMemberGraphql(id: String, peerId: String): ChatChannel = withIO {
    RustGraphqlApi.execute(Mutation.removeChatChannelMember(id, peerId))
}
internal suspend fun acceptInviteGraphql(id: String): GraphQLResponse = withIO {
    try {
        RustGraphqlApi.execute(Mutation.acceptChatChannelInvite(id))
        GraphQLResponse(data = "true")
    } catch (e: Exception) {
        GraphQLResponse(exception = e)
    }
}
internal suspend fun declineInviteGraphql(id: String): Boolean = withIO {
    RustGraphqlApi.execute(Mutation.declineChatChannelInvite(id))
}
internal suspend fun snapshotChannelsGraphql(): List<ChatChannel> = withIO {
    RustGraphqlApi.execute(Query.chatChannels())
}
internal suspend fun channelGraphql(id: String): ChatChannel? {
    return snapshotChannelsGraphql().firstOrNull { it.id == id }
}
