package com.ismartcoding.plain.chat

import com.ismartcoding.plain.chat.data.ChatTarget
import com.ismartcoding.plain.db.DChat
import com.ismartcoding.plain.db.DMessageContent
import com.ismartcoding.plain.enums.ChatStatus
import com.ismartcoding.plain.db.DShare
import com.ismartcoding.plain.features.share.ShareManager
import com.ismartcoding.plain.platform.buildPlatformShareContent
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlin.time.Instant

object ShareSendHelper {
    suspend fun sendContentAsync(targets: List<ChatTarget>, content: DMessageContent): List<DChat> = coroutineScope {
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

    suspend fun buildShareContent(share: DShare, paths: List<String>): DMessageContent =
        buildPlatformShareContent(share, paths)
}
