package com.ismartcoding.plain.features.system

import com.ismartcoding.plain.graphql.models.File as FileEntry

import com.ismartcoding.plain.lib.JsonHelper
import kotlinx.serialization.json.*

internal object SystemFilesHost {
    suspend fun handle(method: String, params: JsonObject): JsonElement = when (method) {
        "systemFileIndexId" -> JsonHelper.jsonEncodeToElement(com.ismartcoding.plain.platform.fileIndexId(params.getValue("path").jsonPrimitive.content))
        "systemFileIndexCount" -> JsonHelper.jsonEncodeToElement(com.ismartcoding.plain.platform.fileIndexCount(params.providerPlan()))
        "systemFileIndexRows" -> JsonHelper.jsonEncodeToElement(com.ismartcoding.plain.platform.fileIndexRows(params.providerPlan(), params.getValue("limit").jsonPrimitive.int, params.getValue("offset").jsonPrimitive.int, com.ismartcoding.plain.features.file.FileSortBy.valueOf(params.getValue("sortBy").jsonPrimitive.content)).map(::fileFacts))
        "fileMetadataFacts" -> {
            val path = params.getValue("path").jsonPrimitive.content
            JsonHelper.jsonEncodeToElement(FileMetadataFacts(
                size = com.ismartcoding.plain.platform.statFile(path)?.size ?: 0,
                mimeType = com.ismartcoding.plain.platform.getContentTypeForPath(path).orEmpty(),
            ))
        }
        "systemMountFacts" -> JsonHelper.jsonEncodeToElement(com.ismartcoding.plain.platform.storageVolumeFacts())
        "imageDecodeBounds" -> JsonHelper.jsonEncodeToElement(com.ismartcoding.plain.platform.readImageBounds(params.getValue("path").jsonPrimitive.content))
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
