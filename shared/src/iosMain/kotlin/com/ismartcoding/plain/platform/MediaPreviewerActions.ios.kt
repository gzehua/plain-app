package com.ismartcoding.plain.platform

internal actual fun sharePreviewFile(path: String, mimeType: String, mediaId: String) = shareFile(path)
internal actual suspend fun useCachedPreviewFile(path: String, consume: suspend (String) -> Unit): Boolean = false
