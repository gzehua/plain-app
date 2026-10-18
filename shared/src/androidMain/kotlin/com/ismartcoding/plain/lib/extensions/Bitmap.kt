package com.ismartcoding.plain.lib.extensions

import android.graphics.Bitmap
import com.ismartcoding.plain.platform.isRPlus
import java.io.ByteArrayOutputStream

@Suppress("DEPRECATION")
fun Bitmap.compress(quality: Int, outputStream: ByteArrayOutputStream) {
    if (isRPlus()) {
        compress(Bitmap.CompressFormat.WEBP_LOSSY, quality, outputStream)
    } else {
        compress(Bitmap.CompressFormat.WEBP, quality, outputStream)
    }
}