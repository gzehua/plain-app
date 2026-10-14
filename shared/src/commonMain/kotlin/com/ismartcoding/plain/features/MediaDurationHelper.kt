package com.ismartcoding.plain.features

import com.ismartcoding.plain.TempData
import com.ismartcoding.plain.enums.DataType
import com.ismartcoding.plain.graphql.models.MediaHostDuration
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.rust.content.gql
import com.ismartcoding.plain.rust.content.gqlIds
import com.ismartcoding.plain.rust.request.RustContentApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.time.Clock
import kotlin.time.Instant

object MediaDurationHelper {
    private val lock = Mutex()
    suspend fun all(): List<MediaHostDuration> = RustContentApi.query("mediaHostDurations { type mediaId durationMs updatedAt path sizeBytes modifiedAt }")
        .getValue("mediaHostDurations").let { JsonHelper.jsonDecodeFromElement<List<MediaHostDuration>>(it) }
    suspend fun restore() = lock.withLock {
        val rows = all()
        withContext(Dispatchers.Main) {
            TempData.mediaDurationMap.clear()
            rows.forEach { TempData.mediaDurationMap["${it.type.name.lowercase()}:${it.mediaId}"] = it }
        }
    }
    suspend fun save(
        mediaType: String,
        mediaId: String,
        durationMs: Long,
        path: String,
        sizeBytes: Long,
        modifiedAt: Instant,
    ) = lock.withLock {
        val type = DataType.valueOf(mediaType.uppercase())
        RustContentApi.mutate("mediaHostSaveDuration(type: ${type.name}, mediaId: ${gql(mediaId)}, durationMs: $durationMs, path: ${gql(path)}, sizeBytes: $sizeBytes, modifiedAt: ${gql(modifiedAt.toString())})")
        TempData.mediaDurationMap["$mediaType:$mediaId"] = MediaHostDuration(type, mediaId, durationMs, Clock.System.now(), path, sizeBytes, modifiedAt)
    }
    fun cachedDuration(
        mediaType: String,
        mediaId: String,
        path: String,
        sizeBytes: Long,
        modifiedAt: Instant,
    ): Long {
        val cached = TempData.mediaDurationMap["$mediaType:$mediaId"] ?: return 0L
        return if (cached.path == path && cached.sizeBytes == sizeBytes && cached.modifiedAt.epochSeconds == modifiedAt.epochSeconds) cached.durationMs else 0L
    }
    suspend fun delete(type: DataType, ids: Collection<String>) = lock.withLock {
        RustContentApi.mutate("mediaHostRemoveDurations(type: ${type.name}, mediaIds: ${gqlIds(ids)})")
        ids.forEach { TempData.mediaDurationMap.remove("${type.name.lowercase()}:$it") }
    }
}
