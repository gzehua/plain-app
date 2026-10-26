package com.ismartcoding.plain.features.media.actions

import com.ismartcoding.plain.graphql.Mutation
import com.ismartcoding.plain.rust.graphql.RustGraphqlApi
import com.ismartcoding.plain.platform.system.DataType
import com.ismartcoding.plain.rust.graphql.selectionQuery
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

object MediaActionClient {
    suspend fun run(type: DataType, action: MediaAction, ids: Collection<String>, fromTrash: Boolean = false, destDir: String = ""): Int {
        require(type in setOf(DataType.AUDIO, DataType.VIDEO, DataType.IMAGE, DataType.DOC)) { "Unsupported media type" }
        if (ids.isEmpty()) return 0
        val query = mediaActionSelection(ids, fromTrash || action == MediaAction.RESTORE)
        val request = Mutation.mediaAction(action, type.name, query, destDir)
        return RustGraphqlApi.graphql(request).getValue(request.field).jsonObject.getValue("affectedCount").jsonPrimitive.int
    }
}

internal fun mediaActionSelection(ids: Collection<String>, fromTrash: Boolean): String {
    return "${selectionQuery(ids)} trash:$fromTrash"
}
