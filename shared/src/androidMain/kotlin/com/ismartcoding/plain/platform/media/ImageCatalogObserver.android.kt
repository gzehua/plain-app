package com.ismartcoding.plain.platform.media

internal actual suspend fun observeImageCatalog(enabled: Boolean) {
    if (enabled) MediaStoreImageObserver.startup() else MediaStoreImageObserver.shutdown()
}
