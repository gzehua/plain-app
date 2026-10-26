package com.ismartcoding.plain.features.chat.channel

import com.ismartcoding.plain.ui.app.MainViewModel
import com.ismartcoding.plain.ui.navigation.Routing
import com.ismartcoding.plain.lib.JsonHelper
import kotlinx.serialization.json.JsonObject

object ChannelSystemMessageReceiver {
    suspend fun applyCommitted(result: JsonObject) {
        val event = JsonHelper.jsonDecodeFromElement<ChannelRuntimeEvent>(result)
        event.invite?.let { invite ->
            val channel = snapshotChannelsGraphql().firstOrNull { it.id == invite.channelId }
            if (channel != null) {
                MainViewModel.pendingChannelInvite.value = Routing.ChannelInviteRequest(
                    channel.id, channel.name, invite.ownerPeerId, invite.ownerPeerName,
                )
            }
        }
        event.cancel?.let { cancel ->
            val stillExists = snapshotChannelsGraphql().any { it.id == cancel.channelId }
            if (!stillExists) {
                val invite = MainViewModel.pendingChannelInvite.value
                if (invite?.channelId == cancel.channelId && invite.ownerPeerId == cancel.ownerPeerId) {
                    MainViewModel.pendingChannelInvite.value = null
                }
            }
        }
    }
}
