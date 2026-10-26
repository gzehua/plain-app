package com.ismartcoding.plain.features.chat.channel

import com.ismartcoding.plain.events.HChannelInviteReceivedEvent
import com.ismartcoding.plain.events.HChannelInviteCanceledEvent
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.lib.sendEvent
import kotlinx.serialization.json.JsonObject

object ChannelSystemMessageReceiver {
    suspend fun applyCommitted(result: JsonObject) {
        val event = JsonHelper.jsonDecodeFromElement<ChannelRuntimeEvent>(result)
        event.invite?.let { invite ->
            val channel = snapshotChannelsGraphql().firstOrNull { it.id == invite.channelId }
            if (channel != null) {
                sendEvent(HChannelInviteReceivedEvent(channel.id, channel.name, invite.ownerPeerId, invite.ownerPeerName))
            }
        }
        event.cancel?.let { cancel ->
            val stillExists = snapshotChannelsGraphql().any { it.id == cancel.channelId }
            if (!stillExists) {
                sendEvent(HChannelInviteCanceledEvent(cancel.channelId, cancel.ownerPeerId))
            }
        }
    }
}
