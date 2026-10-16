package com.ismartcoding.plain.rust.host.provider

import com.ismartcoding.plain.api.graphql.models.File as FileEntry

import com.ismartcoding.plain.lib.JsonHelper
import kotlinx.serialization.json.*

internal object SystemFilesHost {
    suspend fun handle(method: String, params: JsonObject): JsonElement = when (method) {
        "systemFileIndexId" -> JsonHelper.jsonEncodeToElement(com.ismartcoding.plain.platform.system.fileIndexId(params.getValue("path").jsonPrimitive.content))
        "systemFileIndexCount" -> JsonHelper.jsonEncodeToElement(com.ismartcoding.plain.platform.system.fileIndexCount(params.providerPlan()))
        "systemFileIndexRows" -> JsonHelper.jsonEncodeToElement(com.ismartcoding.plain.platform.system.fileIndexRows(params.providerPlan(), params.getValue("limit").jsonPrimitive.int, params.getValue("offset").jsonPrimitive.int, com.ismartcoding.plain.features.files.FileSortBy.valueOf(params.getValue("sortBy").jsonPrimitive.content)).map(::fileFacts))
        "systemResourceMimeType" -> JsonHelper.jsonEncodeToElement(
            com.ismartcoding.plain.platform.getContentTypeForPath(params.getValue("path").jsonPrimitive.content).orEmpty(),
        )
        "systemMountFacts" -> JsonHelper.jsonEncodeToElement(com.ismartcoding.plain.platform.files.storageVolumeFacts())
        "imageDecodeBounds" -> JsonHelper.jsonEncodeToElement(com.ismartcoding.plain.platform.media.readImageBounds(params.getValue("path").jsonPrimitive.content))
        else -> error("Unsupported provider operation")
    }

    /** The `File` contract row. `mediaId` stays an empty string for non-media
     * entries; Rust maps that to `null` rather than carrying the sentinel. */
    private fun fileFacts(file: FileEntry): FileFacts = FileFacts(
        name = file.name,
        path = file.path,
        mediaId = file.mediaId.orEmpty(),
        createdAt = file.createdAt?.toEpochMilliseconds(),
        updatedAt = file.updatedAt.toEpochMilliseconds(),
        size = file.size,
        isDir = file.isDir,
        childCount = file.childCount,
    )

}
