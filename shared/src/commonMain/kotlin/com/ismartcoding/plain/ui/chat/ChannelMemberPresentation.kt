package com.ismartcoding.plain.ui.chat

import com.ismartcoding.plain.models.ChatChannel
import com.ismartcoding.plain.models.Peer

import com.ismartcoding.plain.app.TempData
import com.ismartcoding.plain.models.ChatChannelMember

fun ChatChannelMember.isMe(): Boolean = peerId == TempData.clientId

fun mePeer(): Peer? = com.ismartcoding.plain.ui.app.LocalDeviceState.peer.value

fun ChatChannel.getOwner(peers: Map<String, Peer>): Peer? {
    return if (actions.isOwner) mePeer() else peers[ownerId]
}

fun ChatChannel.isJoined(): Boolean = status == com.ismartcoding.plain.features.chat.ChatChannelStatus.JOINED
fun ChatChannel.joinedMembers(): List<ChatChannelMember> = members.filter { it.isJoined() }
