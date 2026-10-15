package com.ismartcoding.plain.features.mediaactions

import com.ismartcoding.plain.rust.request.*
import com.ismartcoding.plain.rust.event.*
import com.ismartcoding.plain.rust.host.*
import com.ismartcoding.plain.rust.bridge.*
import com.ismartcoding.plain.rust.http.*
import com.ismartcoding.plain.rust.content.*
import com.ismartcoding.plain.enums.DataType
import kotlinx.serialization.json.*

object MediaActionHelper {
    suspend fun run(type: DataType, action: MediaAction, ids: Collection<String>, fromTrash: Boolean = false, destDir: String = ""): Int {
        val capability = when (action) {
            MediaAction.DELETE -> com.ismartcoding.plain.platform.Capability.MEDIA_DELETE
            MediaAction.MOVE -> com.ismartcoding.plain.platform.Capability.MEDIA_MOVE
            MediaAction.TRASH, MediaAction.RESTORE -> com.ismartcoding.plain.platform.Capability.MEDIA_TRASH
        }
        check(capability in com.ismartcoding.plain.platform.getDeviceCapabilities()) { "Unsupported capability ${capability.name}" }
        var affected = 0
        val failed = mutableListOf<String>()
        for (batch in ids.distinct().chunked(500)) {
            val value = RustContentApi.mutate("mediaAction(type: ${type.name}, action: ${action.name}, ids: ${gqlIds(batch)}, fromTrash: $fromTrash, destDir: ${gql(destDir)}) { affectedCount failedIds }").getValue("mediaAction").jsonObject
            affected += value.getValue("affectedCount").jsonPrimitive.int
            val batchFailed = value.getValue("failedIds").jsonArray.map { it.jsonPrimitive.content }.toSet()
            failed.addAll(batchFailed)

        }
        check(failed.isEmpty()) { "${action.name}: ${failed.size} media actions failed ($affected completed)" }
        return affected
    }
}
