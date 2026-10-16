package com.ismartcoding.plain.features.chat

import com.ismartcoding.plain.features.chat.models.ChatChannelRecord
import com.ismartcoding.plain.features.chat.models.PeerRecord
import com.ismartcoding.plain.api.graphql.models.ChatChannel
import com.ismartcoding.plain.api.graphql.models.ChatItem
import com.ismartcoding.plain.api.graphql.models.Peer

data class ReceivedChat(val chat: ChatItem, val peer: PeerRecord, val channel: ChatChannelRecord?)
