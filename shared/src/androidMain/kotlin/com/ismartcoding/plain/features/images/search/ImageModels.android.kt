package com.ismartcoding.plain.features.images.search

import com.ismartcoding.plain.features.images.search.ImageIndexManager

internal actual suspend fun observeImageModels(enabled: Boolean) {
    if (enabled) ImageIndexManager.startup() else ImageIndexManager.shutdown()
}
