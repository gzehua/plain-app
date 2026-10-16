package com.ismartcoding.plain.features.chat.channel

import com.ismartcoding.plain.features.chat.models.ChatChannelRecord
import com.ismartcoding.plain.features.chat.peer.GraphQLError
import com.ismartcoding.plain.features.chat.peer.GraphQLResponse
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.lib.withIO
import com.ismartcoding.plain.rust.PlainRust
import com.ismartcoding.plain.rust.request.gql
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

private const val CHANNEL_FIELDS =
    "id ownerId name members { peerId status } version status createdAt updatedAt"

internal fun decodeChannel(value: JsonElement): ChatChannelRecord =
    JsonHelper.jsonDecodeFromElement(value)

internal suspend fun createChannelGraphql(name: String): ChatChannelRecord = withIO {
    decodeChannel(
        PlainRust.mainMutate(
            "createChatChannel(name: ${gql(name)}) { $CHANNEL_FIELDS }",
            longRunning = true,
        ).getValue("createChatChannel"),
    )
}

internal suspend fun updateChannelGraphql(id: String, name: String): ChatChannelRecord = withIO {
    decodeChannel(
        PlainRust.mainMutate(
            "updateChatChannel(id: ${gql(id)}, name: ${gql(name)}) { $CHANNEL_FIELDS }",
            longRunning = true,
        ).getValue("updateChatChannel"),
    )
}

internal suspend fun deleteChannelGraphql(id: String): Boolean = withIO {
    PlainRust.mainMutate("deleteChatChannel(id: ${gql(id)})")
        .getValue("deleteChatChannel").jsonPrimitive.content == "true"
}

internal suspend fun leaveChannelGraphql(id: String): Boolean = withIO {
    PlainRust.mainMutate("leaveChatChannel(id: ${gql(id)})")
        .getValue("leaveChatChannel").jsonPrimitive.content == "true"
}

internal suspend fun addMemberGraphql(id: String, peerId: String): ChatChannelRecord = withIO {
    decodeChannel(
        PlainRust.mainMutate(
            "addChatChannelMember(id: ${gql(id)}, peerId: ${gql(peerId)}) { $CHANNEL_FIELDS }",
            longRunning = true,
        ).getValue("addChatChannelMember"),
    )
}

internal suspend fun removeMemberGraphql(id: String, peerId: String): ChatChannelRecord = withIO {
    decodeChannel(
        PlainRust.mainMutate(
            "removeChatChannelMember(id: ${gql(id)}, peerId: ${gql(peerId)}) { $CHANNEL_FIELDS }",
            longRunning = true,
        ).getValue("removeChatChannelMember"),
    )
}

internal suspend fun acceptInviteGraphql(id: String): GraphQLResponse = withIO {
    try {
        PlainRust.mainMutate("acceptChatChannelInvite(id: ${gql(id)})", longRunning = true)
        GraphQLResponse(data = "true")
    } catch (e: Exception) {
        GraphQLResponse(exception = e)
    }
}

internal suspend fun declineInviteGraphql(id: String): Boolean = withIO {
    PlainRust.mainMutate("declineChatChannelInvite(id: ${gql(id)})")
        .getValue("declineChatChannelInvite").jsonPrimitive.content == "true"
}

internal suspend fun snapshotChannelsGraphql(): List<ChatChannelRecord> = withIO {
    PlainRust.mainQuery("chatChannels { $CHANNEL_FIELDS }").getValue("chatChannels")
        .let { JsonHelper.jsonDecodeFromElement<List<ChatChannelRecord>>(it) }
}