package com.ismartcoding.plain.extensions

import androidx.compose.ui.unit.IntSize
import com.ismartcoding.plain.graphql.models.Image
import com.ismartcoding.plain.graphql.models.Video

fun Image.getRotatedSize(): IntSize =
    if (rotation == 90 || rotation == 270) IntSize(height, width) else IntSize(width, height)

fun Video.getRotatedSize(): IntSize =
    if (rotation == 90 || rotation == 270) IntSize(height, width) else IntSize(width, height)
