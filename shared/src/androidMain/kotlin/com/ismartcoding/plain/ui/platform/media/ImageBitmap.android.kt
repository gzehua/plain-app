package com.ismartcoding.plain.ui.platform.media

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import android.graphics.BitmapFactory

internal actual fun decodeImageFileBitmap(path: String): ImageBitmap? =
    BitmapFactory.decodeFile(path)?.asImageBitmap()
