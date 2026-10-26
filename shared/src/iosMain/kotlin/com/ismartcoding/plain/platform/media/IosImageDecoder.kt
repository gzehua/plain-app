package com.ismartcoding.plain.platform.media

import coil3.BitmapImage
import coil3.asImage
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Image

internal fun decodeIosImage(bytes: ByteArray): BitmapImage {
    val image = Image.makeFromEncoded(bytes)
    return try {
        Bitmap.makeFromImage(image).apply { setImmutable() }.asImage()
    } finally {
        image.close()
    }
}
