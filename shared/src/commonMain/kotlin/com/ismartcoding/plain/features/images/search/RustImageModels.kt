package com.ismartcoding.plain.features.images.search

import com.ismartcoding.plain.lib.JsonHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.JsonObject
import com.ismartcoding.plain.platform.system.PlatformLock

internal object RustImageModels {
    private val projectionLock = PlatformLock()
    private var version = -1L
    private val state = MutableStateFlow(ImageModelsSnapshot())
    val snapshot = state.asStateFlow()
    fun apply(value: JsonObject) {
        val next = JsonHelper.jsonDecodeFromElement<ImageModelsSnapshot>(value)
        projectionLock.withLock {
            if (next.version < version) return@withLock
            version = next.version
            state.value = next
        }
    }
}
