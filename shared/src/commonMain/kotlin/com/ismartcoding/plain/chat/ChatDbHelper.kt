package com.ismartcoding.plain.chat

import com.ismartcoding.plain.graphql.models.ChatItem
import com.ismartcoding.plain.lib.withIO

object ChatDbHelper {
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

    suspend fun getIdsAsync(query: String): Set<String> = withIO {
        if (query.isEmpty()) return@withIO emptySet()
        RustChatStore.getIds(query)
    }

}
