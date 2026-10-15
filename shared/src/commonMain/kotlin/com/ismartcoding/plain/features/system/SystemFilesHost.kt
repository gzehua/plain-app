package com.ismartcoding.plain.features.system

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
        "systemRecentFileFacts" -> JsonHelper.jsonEncodeToElement(com.ismartcoding.plain.platform.getRecentFiles().map(::fileFacts))
        "systemImageFileInfo" -> fileInfoFacts(method, params)
        "systemVideoFileInfo" -> fileInfoFacts(method, params)
        "systemAudioFileInfo" -> fileInfoFacts(method, params)
        else -> error("Unsupported provider operation")
    }

    /** The `File` contract row. `mediaId` stays an empty string for non-media
     * entries; Rust maps that to `null` rather than carrying the sentinel. */
    private fun fileFacts(file: com.ismartcoding.plain.features.file.DFile): FileFacts = FileFacts(
        name = file.name,
        path = file.path,
        mediaId = file.mediaId,
        createdAt = file.createdAt?.toEpochMilliseconds(),
        updatedAt = file.updatedAt.toEpochMilliseconds(),
        size = file.size,
        isDir = file.isDir,
        childCount = file.childCount,
    )

    private fun fileInfoFacts(method: String, params: JsonObject): JsonElement {
        val path = params.getValue("path").jsonPrimitive.content
        return when (method) {
            "systemImageFileInfo" -> JsonHelper.jsonEncodeToElement(com.ismartcoding.plain.platform.loadImageInfo(path))
            "systemVideoFileInfo" -> JsonHelper.jsonEncodeToElement(com.ismartcoding.plain.platform.loadVideoInfo(path))
            else -> JsonHelper.jsonEncodeToElement(com.ismartcoding.plain.platform.loadAudioInfo(path))
        }
    }
}
