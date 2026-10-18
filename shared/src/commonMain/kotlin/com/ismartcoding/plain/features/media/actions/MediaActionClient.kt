package com.ismartcoding.plain.features.media.actions

import com.ismartcoding.plain.platform.system.DataType
import com.ismartcoding.plain.rust.PlainRust
import com.ismartcoding.plain.rust.request.gql
import com.ismartcoding.plain.rust.request.selectionQuery
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

object MediaActionClient {
    suspend fun run(type: DataType, action: MediaAction, ids: Collection<String>, fromTrash: Boolean = false, destDir: String = ""): Int {
        require(type in setOf(DataType.AUDIO, DataType.VIDEO, DataType.IMAGE, DataType.DOC)) { "Unsupported media type" }
        if (ids.isEmpty()) return 0
        val method = mediaActionMethod(action)
        val query = mediaActionSelection(ids, fromTrash || action == MediaAction.RESTORE)
        val destination = if (action == MediaAction.MOVE) ", destDir: ${gql(destDir)}" else ""
        return PlainRust.mainMutate(
            "$method(type: ${type.name}, query: ${gql(query)}$destination) { affectedCount }",
            longRunning = true,
        ).getValue(method).jsonObject.getValue("affectedCount").jsonPrimitive.int
    }
}

internal fun mediaActionMethod(action: MediaAction): String = when (action) {
    MediaAction.TRASH -> "trashMediaItems"
    MediaAction.RESTORE -> "restoreMediaItems"
    MediaAction.DELETE -> "deleteMediaItems"
    MediaAction.MOVE -> "moveMediaItems"
}

internal fun mediaActionSelection(ids: Collection<String>, fromTrash: Boolean): String {
    return "${selectionQuery(ids)} trash:$fromTrash"
}
