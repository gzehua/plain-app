package com.ismartcoding.plain.features.chat

import com.ismartcoding.plain.graphql.Mutation
import com.ismartcoding.plain.rust.graphql.RustGraphqlApi
import com.ismartcoding.plain.features.chat.data.ChatTarget
import com.ismartcoding.plain.features.chat.data.ChatTargetType
import com.ismartcoding.plain.features.chat.models.MessageContent
import com.ismartcoding.plain.features.chat.models.MessageText
import com.ismartcoding.plain.features.chat.models.MessageType
import com.ismartcoding.plain.features.chat.models.toJSONString
import com.ismartcoding.plain.models.ChatItem
import com.ismartcoding.plain.lib.withIO
import com.ismartcoding.plain.rust.graphql.selectionQuery
import kotlinx.serialization.json.*

object ChatClient {
    suspend fun readDraft(id: String): String = RustGraphqlApi.readSavedValue(draftKey(id), "")
    suspend fun writeDraft(id: String, value: String) = RustGraphqlApi.writeSavedValue(draftKey(id), value)
    private fun draftKey(id: String) = "chat_input_text:${ChatTarget.parseId(id).encodedToId}"

    suspend fun getChatItem(id: String): ChatItem? = ChatQueries.getChatItem(id)

    suspend fun sendContent(target: ChatTarget, content: MessageContent): ChatItem =
        RustChatStore.decodeGraphql(RustGraphqlApi.graphql(Mutation.sendChatItem(target.encodedToId, content.toJSONString()))
            .getValue("sendChatItem").jsonArray.single())

    suspend fun sendText(target: ChatTarget, text: String): ChatItem =
        sendContent(target, MessageContent(MessageType.TEXT, MessageText(text)))

    suspend fun forward(id: String, targets: List<ChatTarget>): List<ChatItem> =
        RustGraphqlApi.graphql(Mutation.forwardChatItems(id, targets.map { it.encodedToId }))
            .getValue("forwardChatItems").jsonArray.map(RustChatStore::decodeGraphql)

    suspend fun deleteQuery(query: String): Int =
        RustGraphqlApi.graphql(Mutation.deleteChatItems(query)).getValue("deleteChatItems").jsonObject.getValue("affectedCount").jsonPrimitive.int

    suspend fun deliver(id: String, recipients: List<String>? = null): ChatItem =
        RustChatStore.decodeGraphql(RustGraphqlApi.graphql(Mutation.deliverChatItem(id, recipients)).getValue("deliverChatItem"))

    suspend fun deleteOne(id: String) {
        RustGraphqlApi.graphql(Mutation.deleteChatItem(id))
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
