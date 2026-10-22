package com.ismartcoding.plain.features.chat.channel

import com.ismartcoding.plain.rust.graphql.RustGraphqlApi

import com.ismartcoding.plain.models.ChatChannel
import com.ismartcoding.plain.features.chat.peer.GraphQLResponse
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.lib.withIO
import com.ismartcoding.plain.rust.graphql.gql
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive

private const val CHANNEL_FIELDS =
    "id ownerId name actions { isOwner canRename canManageMembers canLeave canDelete } members { peerId status } version status createdAt updatedAt"

internal fun decodeChannel(value: JsonElement): ChatChannel =
    JsonHelper.jsonDecodeFromElement(value)

internal suspend fun createChannelGraphql(name: String): ChatChannel = withIO {
    decodeChannel(
        RustGraphqlApi.mutate(
            "createChatChannel(name: ${gql(name)}) { $CHANNEL_FIELDS }",
            longRunning = true,
        ).getValue("createChatChannel"),
    )
}

internal suspend fun updateChannelGraphql(id: String, name: String): ChatChannel = withIO {
    decodeChannel(
        RustGraphqlApi.mutate(
            "updateChatChannel(id: ${gql(id)}, name: ${gql(name)}) { $CHANNEL_FIELDS }",
            longRunning = true,
        ).getValue("updateChatChannel"),
    )
}

internal suspend fun deleteChannelGraphql(id: String): Boolean = withIO {
    RustGraphqlApi.mutate("deleteChatChannel(id: ${gql(id)})")
        .getValue("deleteChatChannel").jsonPrimitive.content == "true"
}

internal suspend fun leaveChannelGraphql(id: String): Boolean = withIO {
    RustGraphqlApi.mutate("leaveChatChannel(id: ${gql(id)})")
        .getValue("leaveChatChannel").jsonPrimitive.content == "true"
}

internal suspend fun addMemberGraphql(id: String, peerId: String): ChatChannel = withIO {
    decodeChannel(
        RustGraphqlApi.mutate(
            "addChatChannelMember(id: ${gql(id)}, peerId: ${gql(peerId)}) { $CHANNEL_FIELDS }",
            longRunning = true,
        ).getValue("addChatChannelMember"),
    )
}

internal suspend fun removeMemberGraphql(id: String, peerId: String): ChatChannel = withIO {
    decodeChannel(
        RustGraphqlApi.mutate(
            "removeChatChannelMember(id: ${gql(id)}, peerId: ${gql(peerId)}) { $CHANNEL_FIELDS }",
            longRunning = true,
        ).getValue("removeChatChannelMember"),
    )
}

internal suspend fun acceptInviteGraphql(id: String): GraphQLResponse = withIO {
    try {
        RustGraphqlApi.mutate("acceptChatChannelInvite(id: ${gql(id)})", longRunning = true)
        GraphQLResponse(data = "true")
    } catch (e: Exception) {
        GraphQLResponse(exception = e)
    }
}

internal suspend fun declineInviteGraphql(id: String): Boolean = withIO {
    RustGraphqlApi.mutate("declineChatChannelInvite(id: ${gql(id)})")
        .getValue("declineChatChannelInvite").jsonPrimitive.content == "true"
}

internal suspend fun snapshotChannelsGraphql(): List<ChatChannel> = withIO {
    RustGraphqlApi.query("chatChannels { $CHANNEL_FIELDS }").getValue("chatChannels")
        .let { JsonHelper.jsonDecodeFromElement<List<ChatChannel>>(it) }
}
internal suspend fun channelGraphql(id: String): com.ismartcoding.plain.models.ChatChannel? {
    return snapshotChannelsGraphql().firstOrNull { it.id == id }
}
