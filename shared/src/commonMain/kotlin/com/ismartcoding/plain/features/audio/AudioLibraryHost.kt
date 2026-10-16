package com.ismartcoding.plain.features.audio

import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.platform.PlatformLock
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
            "audioLibraryResolve" -> JsonHelper.jsonEncodeToElement(provider.resolve(
                params.getValue("ids").jsonArray.map { it.jsonPrimitive.content },
                params.getValue("paths").jsonArray.map { it.jsonPrimitive.content },
            ))
            else -> error("Unknown Rust host operation: $method")
        }
    }

}
