package com.ismartcoding.plain.features.chat

import com.ismartcoding.plain.rust.graphql.RustGraphqlApi

import com.ismartcoding.plain.features.chat.data.ChatTarget
import com.ismartcoding.plain.features.chat.data.ChatTargetType
import com.ismartcoding.plain.features.chat.models.MessageContent
import com.ismartcoding.plain.features.chat.models.MessageText
import com.ismartcoding.plain.features.chat.models.MessageType
import com.ismartcoding.plain.features.chat.models.toJSONString
import com.ismartcoding.plain.models.ChatItem
import com.ismartcoding.plain.lib.withIO
import com.ismartcoding.plain.rust.graphql.gql
import com.ismartcoding.plain.rust.graphql.gqlIds
import com.ismartcoding.plain.rust.graphql.selectionQuery
import kotlinx.serialization.json.*

object ChatClient {

    suspend fun refreshLatestChats() = withIO {
        ChatCacher.load()
    }

    suspend fun getChatItem(id: String): ChatItem? = ChatQueries.getChatItem(id)

    suspend fun sendContent(target: ChatTarget, content: MessageContent): ChatItem =
        RustChatStore.decodeGraphql(RustGraphqlApi.mutate("sendChatItem(target: ${gql(target.encodedToId)}, content: ${gql(content.toJSONString())}) { ${RustChatStore.FIELDS} }", longRunning = true)
            .getValue("sendChatItem").jsonArray.single())

    suspend fun sendText(target: ChatTarget, text: String): ChatItem =
        sendContent(target, MessageContent(MessageType.TEXT, MessageText(text)))

    suspend fun forward(id: String, targets: List<ChatTarget>): List<ChatItem> =
        RustGraphqlApi.mutate("forwardChatItems(id: ${gql(id)}, targets: ${gqlIds(targets.map { it.encodedToId })}) { ${RustChatStore.FIELDS} }", longRunning = true)
            .getValue("forwardChatItems").jsonArray.map(RustChatStore::decodeGraphql)

    suspend fun deleteQuery(query: String): Int =
        RustGraphqlApi.mutate("deleteChatItems(query: ${gql(query)}) { affectedCount }").getValue("deleteChatItems").jsonObject.getValue("affectedCount").jsonPrimitive.int

    suspend fun deliver(id: String, recipients: List<String>? = null): ChatItem =
        RustChatStore.decodeGraphql(RustGraphqlApi.mutate("deliverChatItem(id: ${gql(id)}, recipients: ${recipients?.let(::gqlIds) ?: "null"}) { ${RustChatStore.FIELDS} }", longRunning = true).getValue("deliverChatItem"))

    suspend fun deleteOne(id: String) {
        RustGraphqlApi.mutate("deleteChatItem(id: ${gql(id)})")
    }

    suspend fun deleteByIds(ids: Set<String>) {
        if (ids.isNotEmpty()) deleteQuery(selectionQuery(ids))
    }

    suspend fun clearAllMessages(target: ChatTarget) = withIO {
        require(target.toId.isNotEmpty())
        val field = if (target.type == ChatTargetType.CHANNEL) "channel" else "peer"
        val id = target.toId.replace("\\", "\\\\").replace("\"", "\\\"")
        deleteQuery("$field:=\"$id\"")
    }

}
