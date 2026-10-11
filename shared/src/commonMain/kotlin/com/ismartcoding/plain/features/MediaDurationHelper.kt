package com.ismartcoding.plain.features

import com.ismartcoding.plain.TempData
import com.ismartcoding.plain.rust.request.*
import com.ismartcoding.plain.rust.event.*
import com.ismartcoding.plain.rust.host.*
import com.ismartcoding.plain.rust.bridge.*
import com.ismartcoding.plain.rust.http.*
import com.ismartcoding.plain.rust.content.*
import com.ismartcoding.plain.db.DMediaItem
import com.ismartcoding.plain.enums.DataType
import kotlinx.serialization.json.*
import kotlinx.coroutines.sync.withLock

object MediaDurationHelper {
    private val lock = kotlinx.coroutines.sync.Mutex()
    suspend fun all(): List<DMediaItem> = RustContentApi.query("mediaHostDurations { type mediaId durationMs updatedAt }")
        .getValue("mediaHostDurations").jsonArray.map { value -> value.jsonObject.let {
            DMediaItem(mediaType=it.string("type").lowercase(), mediaId=it.string("mediaId"), durationMs=it.getValue("durationMs").jsonPrimitive.long, updatedAt=it.instant("updatedAt"))
        } }
    suspend fun restore() = lock.withLock {
        val rows = all()
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
            TempData.mediaDurationMap.clear()
            rows.forEach { TempData.mediaDurationMap["${it.mediaType}:${it.mediaId}"] = it.durationMs }
        }
    }
    suspend fun save(mediaType: String, mediaId: String, durationMs: Long) = lock.withLock {
        val type = DataType.valueOf(mediaType.uppercase())
        RustContentApi.mutate("mediaHostSaveDuration(type: ${type.name}, mediaId: ${gql(mediaId)}, durationMs: $durationMs)")
        TempData.mediaDurationMap["$mediaType:$mediaId"] = durationMs
    }
    suspend fun delete(type: DataType, ids: Collection<String>) = lock.withLock {
        RustContentApi.mutate("mediaHostRemoveDurations(type: ${type.name}, mediaIds: ${gqlIds(ids)})")
        ids.forEach { TempData.mediaDurationMap.remove("${type.name.lowercase()}:$it") }
    }
}
