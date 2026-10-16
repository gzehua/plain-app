package com.ismartcoding.plain.platform

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

internal actual fun decodeImageFileBitmap(path: String): ImageBitmap? =
    android.graphics.BitmapFactory.decodeFile(path)?.asImageBitmap()
