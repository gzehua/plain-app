package com.ismartcoding.plain.chat

import com.ismartcoding.plain.chat.models.ChatChannelRecord
import com.ismartcoding.plain.chat.models.PeerRecord
import com.ismartcoding.plain.graphql.models.ChatChannel
import com.ismartcoding.plain.graphql.models.ChatItem
import com.ismartcoding.plain.graphql.models.Peer

data class ReceivedChat(val chat: ChatItem, val peer: PeerRecord, val channel: ChatChannelRecord?)
