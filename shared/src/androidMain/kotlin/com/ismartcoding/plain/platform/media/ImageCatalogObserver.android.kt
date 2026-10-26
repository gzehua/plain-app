package com.ismartcoding.plain.platform.media

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal actual suspend fun observeImageCatalog(enabled: Boolean) {
    if (enabled) MediaStoreImageObserver.startup()
    else withContext(Dispatchers.IO) { MediaStoreImageObserver.shutdown() }
}
