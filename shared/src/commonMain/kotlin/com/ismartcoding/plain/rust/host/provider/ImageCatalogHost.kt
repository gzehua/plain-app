package com.ismartcoding.plain.rust.host.provider

import com.ismartcoding.plain.platform.system.PlatformLock
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

object ImageCatalogHost {
    private val lock = PlatformLock()
    private var provider: ImageIndexProvider = PlatformImageIndexProvider
    fun install(current: ImageIndexProvider): ImageIndexProvider = lock.withLock {
        val previous = provider
        provider = current
        previous
    }
    suspend fun handle(method: String, params: JsonObject): JsonElement = lock.withLock { provider }.handle(method, params)
    suspend fun disconnect() = lock.withLock { provider }.disconnect()
}
