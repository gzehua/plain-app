package com.ismartcoding.plain.features.audio

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
            else -> error("Unknown Rust host operation: $method")
        }
    }

}
