package com.ismartcoding.plain.chat

import com.ismartcoding.plain.chat.data.ChatTarget
import com.ismartcoding.plain.chat.models.MessageContent
import com.ismartcoding.plain.enums.ChatStatus
import com.ismartcoding.plain.features.share.ShareManager
import com.ismartcoding.plain.graphql.models.ChatItem
import com.ismartcoding.plain.graphql.models.ShareRecord
import com.ismartcoding.plain.platform.buildPlatformShareContent
import kotlin.time.Instant
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

object ShareSendHelper {
    suspend fun sendContentAsync(targets: List<ChatTarget>, content: MessageContent): List<ChatItem> = coroutineScope {
        targets.map { target -> async {
            val item = ChatManager.sendContent(target, content)
            ChatViewModel.onMessagesCreated(target, listOf(item), scroll = true)
            item
        } }.awaitAll()
    }

    suspend fun sendFolderShareAsync(targets: List<ChatTarget>, dirPath: String, name: String, expiresAt: Instant?): Boolean {
        if (dirPath.isBlank() || targets.isEmpty()) return false
        val share = ShareManager.createShare(name, listOf(dirPath), readOnly = true, expiresAt = expiresAt)
        return sendContentAsync(targets, buildShareContent(share, listOf(dirPath))).all { it.status == ChatStatus.SENT }
    }

    suspend fun buildShareContent(share: ShareRecord, paths: List<String>): MessageContent =
        buildPlatformShareContent(share, paths)
}
