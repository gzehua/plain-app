package com.ismartcoding.plain.features.images.search

import com.ismartcoding.plain.platform.media.call

internal expect suspend fun observeImageModels(enabled: Boolean)

suspend fun releaseImageModelMemory() {
    com.ismartcoding.plain.features.images.search.RustImageModels.call(com.ismartcoding.plain.features.images.search.ImageModelsCommand.Release)
}
