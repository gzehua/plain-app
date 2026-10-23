package com.ismartcoding.plain.platform.files

import com.ismartcoding.plain.platform.media.MediaStoreFileRow

actual fun queryFileTaskMedia(paths: List<String>): List<MediaStoreFileRow> = error("System media catalog is unavailable")
