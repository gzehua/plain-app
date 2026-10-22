package com.ismartcoding.plain.rust.host.provider

import com.ismartcoding.plain.IData
import com.ismartcoding.plain.lib.JsonHelper
import kotlinx.serialization.json.*
import kotlinx.serialization.Serializable
import com.ismartcoding.plain.platform.media.MediaVolumeRevision
import com.ismartcoding.plain.platform.media.mediaStoreRevision

internal object SystemMediaHost {
    suspend fun handle(method: String, params: JsonObject): JsonElement = when (method) {
        "systemMediaBucketItemFacts" -> {
            val request = JsonHelper.jsonDecodeFromElement<MediaPlanRequest>(params)
            JsonHelper.jsonEncodeToElement(com.ismartcoding.plain.platform.system.mediaBucketItemFacts(
                com.ismartcoding.plain.platform.system.DataType.valueOf(request.dataType), request.plan))
        }
        "systemMediaRows" -> {
            val request = JsonHelper.jsonDecodeFromElement<MediaRowsRequest>(params)
            val includeRevision = request.includeRevision == true
            val before = if (includeRevision) mediaStoreRevision() else null
            val items = com.ismartcoding.plain.platform.media.searchMedia(
                com.ismartcoding.plain.platform.system.DataType.valueOf(request.dataType),
                request.plan, request.limit, request.offset,
                com.ismartcoding.plain.features.files.FileSortBy.valueOf(request.sortBy),
            )
            val rows = items.map { mediaFacts(it) }
            if (includeRevision) JsonHelper.jsonEncodeToElement(MediaRowsFacts(rows, before, mediaStoreRevision()))
            else JsonHelper.jsonEncodeToElement(rows)
        }
        "systemMediaCount" -> {
            val request = JsonHelper.jsonDecodeFromElement<MediaPlanRequest>(params)
            JsonHelper.jsonEncodeToElement(com.ismartcoding.plain.platform.media.countMedia(
                com.ismartcoding.plain.platform.system.DataType.valueOf(request.dataType), request.plan))
        }
        "systemMediaIdsFacts" -> {
            val request = JsonHelper.jsonDecodeFromElement<MediaPlanRequest>(params)
            val type = com.ismartcoding.plain.platform.system.DataType.valueOf(request.dataType)
            val plan = request.plan
            JsonHelper.jsonEncodeToElement(com.ismartcoding.plain.platform.media.getMediaIds(type, plan))
        }
        else -> error("Unsupported provider operation")
    }

    private fun mediaFacts(item: IData): JsonElement = when (item) {
        is com.ismartcoding.plain.models.Audio -> JsonHelper.jsonEncodeToElement(AudioFacts(
            id = item.id,
            title = item.title,
            artist = item.artist,
            path = item.path,
            size = item.size,
            bucketId = item.bucketId,
            durationMs = item.durationMs,
            albumId = item.albumId,
            albumArtUri = com.ismartcoding.plain.ui.platform.media.audioAlbumArtUri(item.albumId),
            createdAt = item.createdAt.toString(),
            updatedAt = item.updatedAt.toString(),
            isFavorite = item.isFavorite,
        ))
        is com.ismartcoding.plain.models.Image -> mediaFacts(
            item.id, item.title, item.path, item.size, item.bucketId, item.createdAt, item.updatedAt,
            durationMs = 0L, takenAt = item.takenAt, isFavorite = item.isFavorite, width = item.width, height = item.height, rotation = item.rotation)
        is com.ismartcoding.plain.models.Video -> mediaFacts(
            item.id, item.title, item.path, item.size, item.bucketId, item.createdAt, item.updatedAt,
            durationMs = item.durationMs, takenAt = item.takenAt, isFavorite = item.isFavorite, width = item.width, height = item.height, rotation = item.rotation)
        is com.ismartcoding.plain.models.Doc -> mediaFacts(
            item.id, item.title, item.path, item.size, item.bucketId, item.createdAt, item.updatedAt,
            durationMs = item.durationMs, takenAt = null, isFavorite = false)
        else -> error("Unsupported media row ${item::class.simpleName}")
    }

    private fun mediaFacts(
        id: String,
        title: String,
        path: String,
        size: Long,
        bucketId: String,
        createdAt: kotlin.time.Instant,
        updatedAt: kotlin.time.Instant,
        durationMs: Long,
        takenAt: kotlin.time.Instant?,
        isFavorite: Boolean,
        width: Int = 0, height: Int = 0, rotation: Int = 0,
    ): JsonElement = JsonHelper.jsonEncodeToElement(MediaFacts(
        id = id,
        title = title,
        path = path,
        size = size,
        bucketId = bucketId,
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString(),
        durationMs = durationMs,
        takenAt = takenAt?.let { it.toString() },
        isFavorite = isFavorite,
        width = width, height = height, rotation = rotation,
    ))

}

@Serializable
internal data class MediaRowsFacts(
    val rows: List<JsonElement>,
    val revisionBefore: List<MediaVolumeRevision>?,
    val revisionAfter: List<MediaVolumeRevision>?,
)
