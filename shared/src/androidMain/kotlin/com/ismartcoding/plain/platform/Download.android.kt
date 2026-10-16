package com.ismartcoding.plain.platform

import android.webkit.MimeTypeMap

actual fun getMimeTypeFromExtension(extension: String): String =
    MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: ""
