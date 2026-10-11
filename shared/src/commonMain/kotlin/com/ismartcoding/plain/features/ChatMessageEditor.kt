package com.ismartcoding.plain.features

import com.ismartcoding.plain.rust.PlainRust
import com.ismartcoding.plain.rust.content.gql
import com.ismartcoding.plain.chat.RustChatStore
import com.ismartcoding.plain.chat.ChatCacher
import com.ismartcoding.plain.db.DChat
import kotlinx.serialization.json.*

object ChatMessageEditor {
    suspend fun updateTextAsync(item: DChat, newText: String): Boolean {
        val result = PlainRust.mainMutate("editChatText(id: ${gql(item.id)}, text: ${gql(newText)}) { changed chat { ${RustChatStore.FIELDS} } }").getValue("editChatText").jsonObject
        val current = RustChatStore.decodeGraphql(result.getValue("chat"))
        item.fromId = current.fromId
        item.toId = current.toId
        item.channelId = current.channelId
        item.createdAt = current.createdAt
        item.status = current.status
        item.statusData = current.statusData
        item.content = current.content
        item.updatedAt = current.updatedAt
        ChatCacher.load()
        return result.getValue("changed").jsonPrimitive.boolean
    }
}
