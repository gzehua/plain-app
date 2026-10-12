package com.ismartcoding.plain.chat

import com.ismartcoding.plain.chat.data.ChatTarget
import com.ismartcoding.plain.chat.models.MessageContent
import com.ismartcoding.plain.chat.models.MessageFile
import com.ismartcoding.plain.chat.models.MessageFiles
import com.ismartcoding.plain.chat.models.MessageImages
import com.ismartcoding.plain.chat.models.MessageType
import com.ismartcoding.plain.chat.models.toJSONString
import com.ismartcoding.plain.graphql.models.ChatItem
import com.ismartcoding.plain.lib.withIO
import com.ismartcoding.plain.rust.PlainRust
import com.ismartcoding.plain.rust.content.gql
import com.ismartcoding.plain.rust.content.gqlIds
import kotlinx.serialization.json.*

object ChatManager {

    suspend fun refreshLatestChats() = withIO {
        ChatCacher.load()
    }

    suspend fun getChatItem(id: String): ChatItem? = ChatDbHelper.getChatItem(id)

    suspend fun getIdsAsync(query: String): Set<String> = ChatDbHelper.getIdsAsync(query)

    suspend fun sendContent(target: ChatTarget, content: MessageContent): ChatItem =
        RustChatStore.decodeGraphql(PlainRust.mainMutate("sendChatItem(target: ${gql(target.encodedToId)}, content: ${gql(content.toJSONString())}) { ${RustChatStore.FIELDS} }", longRunning = true)
            .getValue("sendChatItem").jsonArray.single())

    suspend fun sendText(target: ChatTarget, text: String): ChatItem =
        RustChatStore.decodeGraphql(PlainRust.privateMutate("sendChatText(target: ${gql(target.encodedToId)}, text: ${gql(text)}) { ${RustChatStore.FIELDS} }", longRunning = true).getValue("sendChatText"))

    suspend fun forward(id: String, target: ChatTarget): ChatItem =
        RustChatStore.decodeGraphql(PlainRust.privateMutate("forwardChatItem(id: ${gql(id)}, target: ${gql(target.encodedToId)}) { ${RustChatStore.FIELDS} }", longRunning = true).getValue("forwardChatItem"))

    suspend fun retry(id: String): ChatItem =
        RustChatStore.decodeGraphql(PlainRust.mainMutate("retryChatItem(id: ${gql(id)}) { ${RustChatStore.FIELDS} }").getValue("retryChatItem"))

    suspend fun deleteQuery(query: String): Int =
        PlainRust.mainMutate("deleteChatItems(query: ${gql(query)}) { affectedCount }").getValue("deleteChatItems").jsonObject.getValue("affectedCount").jsonPrimitive.int

    suspend fun deliver(id: String, recipients: List<String>? = null): ChatItem =
        RustChatStore.decodeGraphql(PlainRust.privateMutate("deliverChatItem(id: ${gql(id)}, recipients: ${recipients?.let(::gqlIds) ?: "null"}) { ${RustChatStore.FIELDS} }", longRunning = true).getValue("deliverChatItem"))

    suspend fun insertFilesImmediate(target: ChatTarget, files: List<MessageFile>, isImageVideo: Boolean): ChatItem = withIO {
        val content = if (isImageVideo) MessageContent(MessageType.IMAGES, MessageImages(files))
            else MessageContent(MessageType.FILES, MessageFiles(files))
        val item = RustChatStore.decodeGraphql(PlainRust.privateMutate("createChatItem(target: ${gql(target.encodedToId)}, content: ${gql(content.toJSONString())}) { ${RustChatStore.FIELDS} }").getValue("createChatItem"))
        refreshLatestChats()
        item
    }

    suspend fun updateFilesMessage(
        messageId: String,
        files: List<MessageFile>,
    ): ChatItem? = withIO {
        val content = MessageContent(MessageType.FILES, MessageFiles(files))
        val item = PlainRust.privateMutate("replaceChatFiles(id: ${gql(messageId)}, content: ${gql(content.toJSONString())}) { ${RustChatStore.FIELDS} }", longRunning = true)
            .getValue("replaceChatFiles").takeUnless { it is JsonNull }?.let(RustChatStore::decodeGraphql)
        refreshLatestChats()
        item
    }

    suspend fun deleteOne(id: String) {
        PlainRust.mainMutate("deleteChatItem(id: ${gql(id)})")
    }

    suspend fun deleteByIds(ids: Set<String>) {
        if (ids.isNotEmpty()) deleteQuery("ids:${ids.joinToString(",")}")
    }

    suspend fun clearAllMessages(target: ChatTarget) = withIO {
        PlainRust.privateMutate("clearChatItems(target: ${gql(target.encodedToId)}) { affectedCount }")
    }

}
