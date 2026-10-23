package com.ismartcoding.plain.platform.media

import android.net.Uri
import com.ismartcoding.plain.appContext
import android.graphics.BitmapFactory

internal actual fun readImageBounds(path: String): ImageBounds {
    val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    if (path.startsWith("content://")) {
        appContext.contentResolver.openInputStream(Uri.parse(path))?.use { BitmapFactory.decodeStream(it, null, options) }
    } else BitmapFactory.decodeFile(path, options)
    return ImageBounds(options.outWidth.coerceAtLeast(0), options.outHeight.coerceAtLeast(0))
}
