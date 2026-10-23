package com.ismartcoding.plain.ui.platform.media

import androidx.compose.ui.graphics.ImageBitmap

internal expect fun decodeImageFileBitmap(path: String): ImageBitmap?
