package com.ismartcoding.plain.platform.media

import com.ismartcoding.plain.platform.files.getInternalStoragePath

import com.ismartcoding.plain.platform.system.DataType

actual fun getMediaItemUriString(dataType: DataType, id: String, path: String): String = path

internal actual fun platformDownloadsDirPath(): String = getInternalStoragePath() + "/downloads"
