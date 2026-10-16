package com.ismartcoding.plain.features.media.actions

import com.ismartcoding.plain.api.graphql.models.MediaActionOutcome
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.platform.system.DataType
import com.ismartcoding.plain.rust.PlainRust
import com.ismartcoding.plain.rust.request.gql
import com.ismartcoding.plain.rust.request.gqlIds

object MediaActionClient {
    suspend fun run(type: DataType, action: MediaAction, ids: Collection<String>, fromTrash: Boolean = false, destDir: String = ""): Int {
        val result = JsonHelper.jsonDecodeFromElement<MediaActionOutcome>(PlainRust.mainMutate(
            "mediaAction(type: ${type.name}, action: ${action.name}, ids: ${gqlIds(ids)}, fromTrash: $fromTrash, destDir: ${gql(destDir)}) { affectedCount failedIds }",
            longRunning = true,
        ).getValue("mediaAction"))
        check(result.failedIds.isEmpty()) { "${action.name}: ${result.failedIds.size} media actions failed (${result.affectedCount} completed)" }
        return result.affectedCount
    }
}
