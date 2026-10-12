package com.ismartcoding.plain.chat.channel

import com.ismartcoding.plain.chat.peer.PeerCacher
import com.ismartcoding.plain.events.HChannelInviteReceivedEvent
import com.ismartcoding.plain.events.HChannelInviteCanceledEvent
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.lib.sendEvent
import kotlinx.serialization.json.JsonObject

object ChannelSystemMessageReceiver {
    suspend fun applyCommitted(result: JsonObject) {
        val event = JsonHelper.jsonDecodeFromElement<ChannelRuntimeEvent>(result)
        if (event.changed) {
            PeerCacher.load()
            ChannelCacher.load()
            com.ismartcoding.plain.chat.ChatManager.refreshLatestChats()
        }
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