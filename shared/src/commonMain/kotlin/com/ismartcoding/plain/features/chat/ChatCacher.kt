package com.ismartcoding.plain.features.chat

import com.ismartcoding.plain.api.graphql.models.ChatItem
import com.ismartcoding.plain.lib.withIO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

object ChatCacher {
    private val lock = Mutex()
    val latestChatMap = MutableStateFlow<Map<String, ChatItem>>(emptyMap())

    fun getLatestChat(chatId: String): ChatItem? = latestChatMap.value[chatId]

    suspend fun load() = withIO {
        lock.withLock { latestChatMap.value = RustChatStore.getLatestConversations() }
    }
}
