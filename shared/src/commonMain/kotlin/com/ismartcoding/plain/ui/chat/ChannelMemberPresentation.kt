package com.ismartcoding.plain.ui.chat

import com.ismartcoding.plain.models.ChatChannel
import com.ismartcoding.plain.models.Peer

import com.ismartcoding.plain.app.TempData
import com.ismartcoding.plain.models.ChatChannelMember
import com.ismartcoding.plain.features.chat.ChatChannelStatus
import com.ismartcoding.plain.ui.app.LocalDeviceState

fun ChatChannelMember.isMe(): Boolean = peerId == TempData.clientId

fun mePeer(): Peer? = LocalDeviceState.peer.value

fun ChatChannel.getOwner(peers: Map<String, Peer>): Peer? {
    return if (actions.isOwner) mePeer() else peers[ownerId]
}

fun ChatChannel.isJoined(): Boolean = status == ChatChannelStatus.JOINED
fun ChatChannel.joinedMembers(): List<ChatChannelMember> = members.filter { it.isJoined() }
