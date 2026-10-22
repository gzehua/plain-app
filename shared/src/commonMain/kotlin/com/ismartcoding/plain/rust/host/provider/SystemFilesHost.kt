package com.ismartcoding.plain.rust.host.provider

import com.ismartcoding.plain.features.files.FileSortBy
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.models.File as FileEntry
import com.ismartcoding.plain.platform.files.storageVolumeFacts
import com.ismartcoding.plain.platform.media.readImageBounds
import com.ismartcoding.plain.platform.system.fileIndexCount
import com.ismartcoding.plain.platform.system.fileIndexId
import com.ismartcoding.plain.platform.system.fileIndexRows
import com.ismartcoding.plain.rust.host.*
import kotlinx.serialization.json.*

internal object SystemFilesHost {
    suspend fun handle(method: String, params: JsonObject): JsonElement = when (method) {
        "systemFileIndexId" -> JsonHelper.jsonEncodeToElement(fileIndexId(JsonHelper.jsonDecodeFromElement<PathRequest>(params).path))
        "systemFileIndexCount" -> JsonHelper.jsonEncodeToElement(fileIndexCount(params.providerPlan()))
        "systemFileIndexRows" -> {
            val request = JsonHelper.jsonDecodeFromElement<FileIndexRowsRequest>(params)
            JsonHelper.jsonEncodeToElement(fileIndexRows(
                request.plan, request.limit, request.offset,
                FileSortBy.valueOf(request.sortBy),
            ).map(::fileFacts))
        }
        "systemMountFacts" -> JsonHelper.jsonEncodeToElement(storageVolumeFacts())
        "imageDecodeBounds" -> JsonHelper.jsonEncodeToElement(readImageBounds(JsonHelper.jsonDecodeFromElement<PathRequest>(params).path))
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
