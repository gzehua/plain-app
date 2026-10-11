package com.ismartcoding.plain.chat.channel

import com.ismartcoding.plain.chat.ChatCacher
import com.ismartcoding.plain.chat.models.ChatChannelRecord
import com.ismartcoding.plain.graphql.models.ChatChannel
import com.ismartcoding.plain.graphql.models.ChatItem
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

    val channelsMap = MutableStateFlow<Map<String, ChatChannelRecord>>(emptyMap())

    val channels: StateFlow<List<ChatChannelRecord>> = combine(channelsMap, ChatCacher.latestChatMap) { c, chatCache ->
        c.values.sortedWith(
            compareByDescending<ChatChannelRecord> { chatCache[it.id]?.createdAt ?: Instant.DISTANT_PAST }
                .thenBy { it.name.toSortName() },
        )
    }.stateIn(scope, SharingStarted.Eagerly, emptyList())

    /** Read-only channel view; use [ChannelManager] for mutations. */
    fun getChannel(channelId: String): ChatChannelRecord? = channelsMap.value[channelId]

    suspend fun load() = withIO {
        val channels = RustChannelStore.getAll()
        val runtimeMap = channels.associate { channel ->
            channel.id to channel
        }
        channelsMap.value = runtimeMap
    }
}
