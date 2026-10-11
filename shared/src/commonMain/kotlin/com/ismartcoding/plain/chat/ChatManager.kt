package com.ismartcoding.plain.chat

import com.ismartcoding.plain.lib.withIO
import com.ismartcoding.plain.chat.data.ChatTarget
import com.ismartcoding.plain.db.DChat
import com.ismartcoding.plain.db.DMessageContent
import com.ismartcoding.plain.db.DMessageFile
import com.ismartcoding.plain.db.DMessageFiles
import com.ismartcoding.plain.db.DMessageImages
import com.ismartcoding.plain.db.MessageType
import com.ismartcoding.plain.db.toJSONString
import com.ismartcoding.plain.rust.PlainRust
import com.ismartcoding.plain.rust.content.gql
import com.ismartcoding.plain.rust.content.gqlIds
import kotlinx.serialization.json.*

object ChatManager {

    suspend fun refreshLatestChats() = withIO {
        ChatCacher.load()
    }

    suspend fun getChatItem(id: String): DChat? = ChatDbHelper.getChatItem(id)

    suspend fun getIdsAsync(query: String): Set<String> = ChatDbHelper.getIdsAsync(query)

    suspend fun sendContent(target: ChatTarget, content: DMessageContent): DChat =
        RustChatStore.decodeGraphql(PlainRust.mainMutate("sendChatItem(target: ${gql(target.encodedToId)}, content: ${gql(content.toJSONString())}) { ${RustChatStore.FIELDS} }", longRunning = true)
            .getValue("sendChatItem").jsonArray.single())

    suspend fun sendText(target: ChatTarget, text: String): DChat =
        RustChatStore.decodeGraphql(PlainRust.mainMutate("sendChatText(target: ${gql(target.encodedToId)}, text: ${gql(text)}) { ${RustChatStore.FIELDS} }", longRunning = true).getValue("sendChatText"))

    suspend fun forward(id: String, target: ChatTarget): DChat =
        RustChatStore.decodeGraphql(PlainRust.mainMutate("forwardChatItem(id: ${gql(id)}, target: ${gql(target.encodedToId)}) { ${RustChatStore.FIELDS} }", longRunning = true).getValue("forwardChatItem"))

    suspend fun retry(id: String): DChat =
        RustChatStore.decodeGraphql(PlainRust.mainMutate("retryChatItem(id: ${gql(id)}) { ${RustChatStore.FIELDS} }").getValue("retryChatItem"))

    suspend fun deleteQuery(query: String): Int =
        PlainRust.mainMutate("deleteChatItems(query: ${gql(query)}) { affectedCount }").getValue("deleteChatItems").jsonObject.getValue("affectedCount").jsonPrimitive.int

    suspend fun deliver(id: String, recipients: List<String>? = null): DChat =
        RustChatStore.decodeGraphql(PlainRust.mainMutate("deliverChatItem(id: ${gql(id)}, recipients: ${recipients?.let(::gqlIds) ?: "null"}) { ${RustChatStore.FIELDS} }", longRunning = true).getValue("deliverChatItem"))

    suspend fun insertFilesImmediate(target: ChatTarget, files: List<DMessageFile>, isImageVideo: Boolean): DChat = withIO {
        val content = if (isImageVideo) DMessageContent(MessageType.IMAGES, DMessageImages(files))
            else DMessageContent(MessageType.FILES, DMessageFiles(files))
        val item = RustChatStore.decodeGraphql(PlainRust.mainMutate("createChatItem(target: ${gql(target.encodedToId)}, content: ${gql(content.toJSONString())}) { ${RustChatStore.FIELDS} }").getValue("createChatItem"))
        refreshLatestChats()
        item
    }

    suspend fun updateFilesMessage(
        messageId: String,
        files: List<DMessageFile>,
    ): DChat? = withIO {
        val content = DMessageContent(MessageType.FILES, DMessageFiles(files))
        val item = PlainRust.mainMutate("replaceChatFiles(id: ${gql(messageId)}, content: ${gql(content.toJSONString())}) { ${RustChatStore.FIELDS} }", longRunning = true)
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
        PlainRust.mainMutate("clearChatItems(target: ${gql(target.encodedToId)}) { affectedCount }")
    }

}
