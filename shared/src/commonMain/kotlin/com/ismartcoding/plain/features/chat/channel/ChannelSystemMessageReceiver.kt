package com.ismartcoding.plain.features.chat.channel

import com.ismartcoding.plain.ui.app.MainViewModel
import com.ismartcoding.plain.ui.navigation.Routing

object ChannelSystemMessageReceiver {
    internal suspend fun applyCommitted(event: ChannelRuntimeEvent) {
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
