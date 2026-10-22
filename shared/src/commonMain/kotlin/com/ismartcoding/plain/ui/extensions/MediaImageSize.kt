package com.ismartcoding.plain.ui.extensions

import androidx.compose.ui.unit.IntSize
import com.ismartcoding.plain.models.Image
import com.ismartcoding.plain.models.Video

fun Image.getRotatedSize(): IntSize =
    if (rotation == 90 || rotation == 270) IntSize(height, width) else IntSize(width, height)

fun Video.getRotatedSize(): IntSize =
    if (rotation == 90 || rotation == 270) IntSize(height, width) else IntSize(width, height)
