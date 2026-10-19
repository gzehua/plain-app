package com.ismartcoding.plain.platform.files

actual fun queryFileTaskMedia(paths: List<String>): List<com.ismartcoding.plain.platform.media.MediaStoreFileRow> = error("System media catalog is unavailable")
