package com.ismartcoding.plain.ui.chat

import com.ismartcoding.plain.models.ChatChannel

import com.ismartcoding.plain.Prefs
import com.ismartcoding.plain.models.ChatChannelMember
import com.ismartcoding.plain.features.chat.ChatChannelStatus

fun ChatChannelMember.isMe(): Boolean = peerId == Prefs.clientId.value

fun ChatChannel.isJoined(): Boolean = status == ChatChannelStatus.JOINED
fun ChatChannel.joinedMembers(): List<ChatChannelMember> = members.filter { it.isJoined() }
