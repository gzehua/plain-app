package com.ismartcoding.plain.ui.chat

import com.ismartcoding.plain.api.graphql.models.ChatChannel
import com.ismartcoding.plain.api.graphql.models.PeerRecord

import com.ismartcoding.plain.app.TempData
import com.ismartcoding.plain.ui.chat.peer.PeerCacher
import com.ismartcoding.plain.api.graphql.models.ChatChannelMember

fun ChatChannelMember.isMe(): Boolean = peerId == TempData.clientId

fun mePeer(): PeerRecord? = com.ismartcoding.plain.ui.app.LocalDeviceState.peer.value

fun ChatChannel.getOwner(): PeerRecord? {
    return if (actions.isOwner) mePeer() else PeerCacher.getPeer(ownerId)
}

fun ChatChannel.isJoined(): Boolean = status == com.ismartcoding.plain.features.chat.ChatChannelStatus.JOINED
fun ChatChannel.joinedMembers(): List<ChatChannelMember> = members.filter { it.isJoined() }
