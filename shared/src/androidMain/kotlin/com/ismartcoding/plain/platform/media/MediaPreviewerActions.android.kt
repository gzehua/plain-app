package com.ismartcoding.plain.platform.media

import coil3.imageLoader
import com.ismartcoding.plain.appContextValue

internal actual suspend fun useCachedPreviewFile(path: String, consume: suspend (String) -> Unit): Boolean {
    val context = appContextValue ?: return false
    val snapshot = context.imageLoader.diskCache?.openSnapshot(path) ?: return false
    snapshot.use { consume(it.data.toString()) }
    return true
}
