package com.ismartcoding.plain.features.chat

import com.ismartcoding.plain.ui.chat.ChatViewModel

import com.ismartcoding.plain.features.chat.data.ChatTarget
import com.ismartcoding.plain.features.chat.models.MessageContent
import com.ismartcoding.plain.features.chat.ChatStatus
import com.ismartcoding.plain.features.shares.SharesClient
import com.ismartcoding.plain.api.graphql.models.ChatItem
import com.ismartcoding.plain.api.graphql.models.ShareRecord
import kotlin.time.Instant
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

object ShareSendHelper {
    suspend fun sendContentAsync(targets: List<ChatTarget>, content: MessageContent): List<ChatItem> = coroutineScope {
        targets.map { target -> async {
            val item = ChatClient.sendContent(target, content)
            ChatViewModel.onMessagesCreated(target, listOf(item), scroll = true)
            item
        } }.awaitAll()
    }

    suspend fun sendFolderShareAsync(targets: List<ChatTarget>, dirPath: String, name: String, expiresAt: Instant?): Boolean {
        if (dirPath.isBlank() || targets.isEmpty()) return false
        val share = SharesClient.createShare(name, listOf(dirPath), readOnly = true, expiresAt = expiresAt)
        return sendContentAsync(targets, buildShareContent(share)).all { it.status == ChatStatus.SENT }
    }

    suspend fun buildShareContent(share: ShareRecord): MessageContent =
        SharesClient.messageContent(share.id)
}
