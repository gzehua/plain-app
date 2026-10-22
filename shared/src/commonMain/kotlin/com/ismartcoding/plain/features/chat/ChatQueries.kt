package com.ismartcoding.plain.features.chat

import com.ismartcoding.plain.models.ChatItem
import com.ismartcoding.plain.lib.withIO

object ChatQueries {
    suspend fun getChatItem(id: String): ChatItem? = withIO {
        RustChatStore.getById(id)
    }

    suspend fun searchAsync(query: String, limit: Int, offset: Int): List<ChatItem> = withIO {
        if (query.isEmpty()) return@withIO emptyList()
        RustChatStore.search(query, limit, offset)
    }

    suspend fun countAsync(query: String): Int = withIO {
        if (query.isEmpty()) return@withIO 0
        RustChatStore.count(query)
    }

}
