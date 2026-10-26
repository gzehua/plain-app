package com.ismartcoding.plain.features.chat

import com.ismartcoding.plain.graphql.Mutation
import com.ismartcoding.plain.rust.graphql.RustGraphqlApi
import com.ismartcoding.plain.models.ChatItem
import com.ismartcoding.plain.models.ChatTextEdit
import com.ismartcoding.plain.lib.JsonHelper

object ChatMessageEditor {
    suspend fun updateTextAsync(item: ChatItem, newText: String): Boolean {
        val result = JsonHelper.jsonDecodeFromElement<ChatTextEdit>(RustGraphqlApi.graphql(Mutation.editChatText(item.id, newText)).getValue("editChatText"))
        val current = result.chat
        item.fromId = current.fromId
        item.toId = current.toId
        item.channelId = current.channelId
        item.createdAt = current.createdAt
        item.status = current.status
        item.statusData = current.statusData
        item.content = current.content
        item.updatedAt = current.updatedAt
        return result.changed
    }
}
