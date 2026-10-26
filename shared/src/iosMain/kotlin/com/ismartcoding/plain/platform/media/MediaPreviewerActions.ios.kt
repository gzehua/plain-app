package com.ismartcoding.plain.platform.media

internal actual suspend fun useCachedPreviewFile(path: String, consume: suspend (String) -> Unit): Boolean = false
