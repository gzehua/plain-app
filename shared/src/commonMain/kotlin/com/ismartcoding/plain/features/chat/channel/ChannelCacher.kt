package com.ismartcoding.plain.features.chat.channel

import com.ismartcoding.plain.features.chat.ChatCacher
import com.ismartcoding.plain.api.graphql.models.ChatChannel
import com.ismartcoding.plain.api.graphql.models.ChatItem
import com.ismartcoding.plain.lib.extensions.toSortName
import com.ismartcoding.plain.lib.withIO
import kotlin.time.Instant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

object ChannelCacher {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val channelsMap = MutableStateFlow<Map<String, ChatChannel>>(emptyMap())

    val channels: StateFlow<List<ChatChannel>> = combine(channelsMap, ChatCacher.latestChatMap) { c, chatCache ->
        c.values.sortedWith(
            compareByDescending<ChatChannel> { chatCache[it.id]?.createdAt ?: Instant.DISTANT_PAST }
                .thenBy { it.name.toSortName() },
        )
    }.stateIn(scope, SharingStarted.Eagerly, emptyList())

    /** Read-only channel view; use [ChannelManager] for mutations. */
    fun getChannel(channelId: String): ChatChannel? = channelsMap.value[channelId]

    suspend fun load() = withIO {
        val channels = snapshotChannelsGraphql()
        val runtimeMap = channels.associate { channel ->
            channel.id to channel
        }
        channelsMap.value = runtimeMap
    }
}
