package com.ismartcoding.plain.ui.page.appfiles

import com.ismartcoding.plain.chat.models.MessageFile
import com.ismartcoding.plain.chat.models.MessageFiles
import com.ismartcoding.plain.chat.models.MessageImages
import com.ismartcoding.plain.graphql.models.AppFile
import com.ismartcoding.plain.graphql.models.ChatItem
import com.ismartcoding.plain.helpers.AppFileStore
import com.ismartcoding.plain.lib.withIO
import com.ismartcoding.plain.platform.getExtensionFromMimeType

object AppFileDisplayNameHelper {
    fun buildNameMap(chats: List<ChatItem>): Map<String, String> {
        val map = mutableMapOf<String, String>()
        chats
            .sortedByDescending { it.createdAt }
            .forEach { chat ->
                when (val value = chat.messageContent.value) {
                    is MessageFiles -> bindItems(value.items, map)
                    is MessageImages -> bindItems(value.items, map)
                }
            }
        return map
    }

    fun resolveDisplayName(file: AppFile, nameMap: Map<String, String>): String {
        val fromChat = nameMap[file.id].orEmpty().trim()
        if (fromChat.isNotEmpty()) return fromChat
        val ext = getExtensionFromMimeType(file.mimeType)
        return if (ext.isNotEmpty()) "file.$ext" else "file"
    }

    suspend fun resolveDisplayNameByPath(path: String, title: String): String = withIO {
        if (title.isNotEmpty()) return@withIO title
        val fileName = path.substringAfterLast('/').substringBeforeLast('.')
        AppFileStore.displayName(fileName).ifEmpty { fileName }
    }

    private fun bindItems(items: List<MessageFile>, map: MutableMap<String, String>) {
        items.forEach { item ->
            if (item.isFidFile() && item.fileName.isNotEmpty()) {
                val hash = item.localFileId().substringBefore(".")
                if (!map.containsKey(hash)) {
                    map[hash] = item.fileName
                }
            }
        }
    }
}
