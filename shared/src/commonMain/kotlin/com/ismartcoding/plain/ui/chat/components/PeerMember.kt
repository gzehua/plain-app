package com.ismartcoding.plain.ui.chat.components

import com.ismartcoding.plain.models.ChatChannelMember
import com.ismartcoding.plain.models.Peer
import com.ismartcoding.plain.platform.system.DeviceType

data class PeerMember(
    val member: ChatChannelMember,
    val name: String,
    val deviceType: DeviceType,
    val peer: Peer? = null,
    val isOwner: Boolean = false,
) {
    val id: String get() = member.peerId
    val isSelf: Boolean get() = peer == null
    fun isJoined(): Boolean = member.isJoined()
    fun isPending(): Boolean = member.isPending()
    fun displayName(): String = name
}
