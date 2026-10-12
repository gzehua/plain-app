package com.ismartcoding.plain.features

import com.ismartcoding.plain.chat.ChatCacher
import com.ismartcoding.plain.chat.RustChatStore
import com.ismartcoding.plain.graphql.models.ChatItem
import com.ismartcoding.plain.graphql.models.ChatTextEdit
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.rust.PlainRust
import com.ismartcoding.plain.rust.content.gql

object ChatMessageEditor {
    suspend fun updateTextAsync(item: ChatItem, newText: String): Boolean {
        val result = JsonHelper.jsonDecodeFromElement<ChatTextEdit>(PlainRust.privateMutate("editChatText(id: ${gql(item.id)}, text: ${gql(newText)}) { changed chat { ${RustChatStore.FIELDS} } }").getValue("editChatText"))
        val current = result.chat
        item.fromId = current.fromId
        item.toId = current.toId
        item.channelId = current.channelId
        item.createdAt = current.createdAt
        item.status = current.status
        item.statusData = current.statusData
        item.content = current.content
        item.updatedAt = current.updatedAt
        ChatCacher.load()
        return result.changed
    }
}
