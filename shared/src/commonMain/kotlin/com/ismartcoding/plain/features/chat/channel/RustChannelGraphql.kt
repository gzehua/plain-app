package com.ismartcoding.plain.features.chat.channel

import com.ismartcoding.plain.graphql.Mutation
import com.ismartcoding.plain.graphql.Query
import com.ismartcoding.plain.rust.graphql.RustGraphqlApi
import com.ismartcoding.plain.models.ChatChannel
import com.ismartcoding.plain.features.chat.peer.GraphQLResponse
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.lib.withIO
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive

internal fun decodeChannel(value: JsonElement): ChatChannel =
    JsonHelper.jsonDecodeFromElement(value)

internal suspend fun createChannelGraphql(name: String): ChatChannel = withIO {
    decodeChannel(
        RustGraphqlApi.graphql(
            Mutation.createChatChannel(name),
        ).getValue("createChatChannel"),
    )
}

internal suspend fun updateChannelGraphql(id: String, name: String): ChatChannel = withIO {
    decodeChannel(
        RustGraphqlApi.graphql(
            Mutation.updateChatChannel(id, name),
        ).getValue("updateChatChannel"),
    )
}

internal suspend fun deleteChannelGraphql(id: String): Boolean = withIO {
    RustGraphqlApi.graphql(Mutation.deleteChatChannel(id))
        .getValue("deleteChatChannel").jsonPrimitive.content == "true"
}

internal suspend fun leaveChannelGraphql(id: String): Boolean = withIO {
    RustGraphqlApi.graphql(Mutation.leaveChatChannel(id))
        .getValue("leaveChatChannel").jsonPrimitive.content == "true"
}

internal suspend fun addMemberGraphql(id: String, peerId: String): ChatChannel = withIO {
    decodeChannel(
        RustGraphqlApi.graphql(
            Mutation.addChatChannelMember(id, peerId),
        ).getValue("addChatChannelMember"),
    )
}

internal suspend fun removeMemberGraphql(id: String, peerId: String): ChatChannel = withIO {
    decodeChannel(
        RustGraphqlApi.graphql(
            Mutation.removeChatChannelMember(id, peerId),
        ).getValue("removeChatChannelMember"),
    )
}

internal suspend fun acceptInviteGraphql(id: String): GraphQLResponse = withIO {
    try {
        RustGraphqlApi.graphql(Mutation.acceptChatChannelInvite(id))
        GraphQLResponse(data = "true")
    } catch (e: Exception) {
        GraphQLResponse(exception = e)
    }
}

internal suspend fun declineInviteGraphql(id: String): Boolean = withIO {
    RustGraphqlApi.graphql(Mutation.declineChatChannelInvite(id))
        .getValue("declineChatChannelInvite").jsonPrimitive.content == "true"
}

internal suspend fun snapshotChannelsGraphql(): List<ChatChannel> = withIO {
    RustGraphqlApi.graphql(Query.chatChannels()).getValue("chatChannels")
        .let { JsonHelper.jsonDecodeFromElement<List<ChatChannel>>(it) }
}
internal suspend fun channelGraphql(id: String): ChatChannel? {
    return snapshotChannelsGraphql().firstOrNull { it.id == id }
}
