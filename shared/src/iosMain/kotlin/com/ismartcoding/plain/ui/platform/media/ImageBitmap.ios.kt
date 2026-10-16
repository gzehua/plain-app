package com.ismartcoding.plain.ui.platform.media

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asComposeImageBitmap
import com.ismartcoding.plain.lib.toByteArray
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Image
import platform.Foundation.NSFileManager

internal actual fun decodeImageFileBitmap(path: String): ImageBitmap? {
    val bytes = NSFileManager.defaultManager.contentsAtPath(path)?.toByteArray() ?: return null
    val image = Image.makeFromEncoded(bytes)
    return try {
        Bitmap.makeFromImage(image).asComposeImageBitmap()
    } finally {
        image.close()
    }
}
