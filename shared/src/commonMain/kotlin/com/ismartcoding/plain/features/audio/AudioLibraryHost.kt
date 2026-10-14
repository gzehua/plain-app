package com.ismartcoding.plain.features.audio

import com.ismartcoding.plain.features.file.FileSortBy
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.platform.PlatformLock
import com.ismartcoding.plain.rust.content.string
import kotlinx.serialization.json.*

object AudioLibraryHost {
    private val lock = PlatformLock()
    private var currentProvider: AudioLibraryProvider = PlatformAudioLibraryProvider
    fun install(provider: AudioLibraryProvider): AudioLibraryProvider = lock.withLock {
        val previous = currentProvider
        currentProvider = provider
        previous
    }
    suspend fun handle(method: String, params: JsonObject): JsonElement {
        val provider = lock.withLock { currentProvider }
        return when (method) {
            "audioMetadata" -> {
                val path = params.string("path")
                val row = provider.resolve(emptyList(), listOf(path)).firstOrNull()
                    ?: AudioMedia(provider.metadata(path), null)
                JsonHelper.jsonEncodeToElement(row)
            }
            "audioLibraryResolve" -> JsonHelper.jsonEncodeToElement(provider.resolve(
                params.getValue("ids").jsonArray.map { it.jsonPrimitive.content },
                params.getValue("paths").jsonArray.map { it.jsonPrimitive.content },
            ))
            "audioLibraryCount" -> JsonPrimitive(provider.count())
            "audioLibraryPath" -> provider.page(params.getValue("offset").jsonPrimitive.int, 1, sort(params)).firstOrNull()?.path?.let(::JsonPrimitive) ?: JsonNull
            "audioLibraryPage" -> {
                val rows = provider.page(params.getValue("offset").jsonPrimitive.int, params.getValue("limit").jsonPrimitive.int, sort(params))
                JsonHelper.jsonEncodeToElement(provider.resolve(emptyList(), rows.map { it.path }))
            }
            "audioLibraryLocate" -> JsonPrimitive(locate(provider, params.string("path"), sort(params)))
            "audioLibraryContains" -> JsonPrimitive(provider.contains(params.string("path")))
            else -> error("Unknown Rust host operation: $method")
        }
    }

    private fun sort(params: JsonObject): FileSortBy = FileSortBy.valueOf(params.string("sortBy"))

    private suspend fun locate(provider: AudioLibraryProvider, path: String, sort: FileSortBy): Int {
        val total = provider.count()
        var offset = 0
        while (offset < total) {
            val rows = provider.page(offset, 500, sort)
            if (rows.isEmpty()) break
            val index = rows.indexOfFirst { it.path == path }
            if (index >= 0) return offset + index
            offset += rows.size
        }
        return -1
    }
}
