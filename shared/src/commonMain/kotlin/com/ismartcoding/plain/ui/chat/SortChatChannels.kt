package com.ismartcoding.plain.ui.chat

import com.ismartcoding.plain.lib.extensions.toSortName
import com.ismartcoding.plain.models.ChatChannel
import com.ismartcoding.plain.models.ChatItem
import kotlin.time.Instant

internal fun sortChatChannels(
    channels: List<ChatChannel>,
    latestChats: Map<String, ChatItem>,
): List<ChatChannel> = channels.sortedWith(
    compareByDescending<ChatChannel> { latestChats[it.id]?.createdAt ?: Instant.DISTANT_PAST }
        .thenBy { it.name.toSortName() },
)
