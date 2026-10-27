package com.ismartcoding.plain.features.images.search

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.ismartcoding.plain.platform.system.PlatformLock

internal object RustImageModels {
    private val projectionLock = PlatformLock()
    private var version = -1L
    private val state = MutableStateFlow(ImageModelsSnapshot())
    val snapshot = state.asStateFlow()
    fun apply(next: ImageModelsSnapshot) {
        projectionLock.withLock {
            if (next.version < version) return@withLock
            version = next.version
            state.value = next
        }
    }
}
