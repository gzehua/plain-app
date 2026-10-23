@file:OptIn(ExperimentalForeignApi::class)

package com.ismartcoding.plain.platform.media

import com.ismartcoding.plain.lib.logcat.LogCat
import kotlinx.cinterop.useContents
import platform.UIKit.UIImage
import kotlinx.cinterop.ExperimentalForeignApi

internal actual fun readImageBounds(path: String): ImageBounds = try {
    UIImage.imageWithContentsOfFile(path)?.size?.useContents {
        ImageBounds(width.toInt().coerceAtLeast(0), height.toInt().coerceAtLeast(0))
    } ?: ImageBounds(0, 0)
} catch (error: Exception) {
    LogCat.e("Image bounds read failed: ${error.message}")
    ImageBounds(0, 0)
}
