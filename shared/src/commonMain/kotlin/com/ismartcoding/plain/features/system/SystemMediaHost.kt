package com.ismartcoding.plain.features.system

import com.ismartcoding.plain.lib.JsonHelper
import kotlinx.serialization.json.*

internal object SystemMediaHost {
    suspend fun handle(method: String, params: JsonObject): JsonElement = when (method) {
        "systemMediaBucketItemFacts" -> JsonHelper.jsonEncodeToElement(
            com.ismartcoding.plain.platform.mediaBucketItemFacts(mediaDataType(params)).map { item ->
                MediaBucketItemFacts(
                    id = item.id,
                    name = item.name,
                    size = item.size,
                    path = item.path,
                    sortName = item.sortName,
                )
        })
        "systemMediaRows" -> {
            val items = com.ismartcoding.plain.platform.searchMedia(
                mediaDataType(params),
                params.providerPlan(),
                params.getValue("limit").jsonPrimitive.int,
                params.getValue("offset").jsonPrimitive.int,
                fileSortBy(params),
            )
            JsonHelper.jsonEncodeToElement(items.map { mediaFacts(it) })
        }
        "systemMediaCount" -> JsonHelper.jsonEncodeToElement(com.ismartcoding.plain.platform.countMedia(
            mediaDataType(params), params.providerPlan()))
        "systemMediaIdsFacts" -> {
            val type = mediaDataType(params)
            val plan = params.providerPlan()
            JsonHelper.jsonEncodeToElement(com.ismartcoding.plain.platform.getMediaIds(type, plan))
        }
        else -> error("Unsupported provider operation")
    }

    private fun mediaFacts(item: com.ismartcoding.plain.db.IData): JsonElement = when (item) {
        is com.ismartcoding.plain.audio.DAudio -> JsonHelper.jsonEncodeToElement(AudioFacts(
            id = item.id,
            title = item.title,
            artist = item.artist,
            path = item.path,
            size = item.size,
            bucketId = item.bucketId,
            durationMs = item.durationMs,
            albumId = item.albumId,
            albumFileId = com.ismartcoding.plain.platform.getAudioAlbumArtFileId(item),
            createdAt = item.createdAt.toString(),
            updatedAt = item.updatedAt.toString(),
            isFavorite = item.isFavorite,
        ))
        is com.ismartcoding.plain.data.DImage -> mediaFacts(
            item.id, item.title, item.path, item.size, item.bucketId, item.createdAt, item.updatedAt,
            durationMs = 0L, takenAt = item.takenAt, isFavorite = item.isFavorite, width = item.width, height = item.height, rotation = item.rotation)
        is com.ismartcoding.plain.data.DVideo -> mediaFacts(
            item.id, item.title, item.path, item.size, item.bucketId, item.createdAt, item.updatedAt,
            durationMs = item.durationMs, takenAt = item.takenAt, isFavorite = item.isFavorite, width = item.width, height = item.height, rotation = item.rotation)
        is com.ismartcoding.plain.data.DDoc -> mediaFacts(
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

    private fun fileSortBy(params: JsonObject) =
        com.ismartcoding.plain.features.file.FileSortBy.valueOf(params.getValue("sortBy").jsonPrimitive.content)
}
