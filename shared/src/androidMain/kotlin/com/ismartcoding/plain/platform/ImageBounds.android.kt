package com.ismartcoding.plain.platform

import android.net.Uri
import com.ismartcoding.plain.appContext

internal actual fun readImageBounds(path: String): ImageBounds {
    val options = android.graphics.BitmapFactory.Options().apply { inJustDecodeBounds = true }
    if (path.startsWith("content://")) {
        appContext.contentResolver.openInputStream(Uri.parse(path))?.use { android.graphics.BitmapFactory.decodeStream(it, null, options) }
    } else android.graphics.BitmapFactory.decodeFile(path, options)
    return ImageBounds(options.outWidth.coerceAtLeast(0), options.outHeight.coerceAtLeast(0))
}
