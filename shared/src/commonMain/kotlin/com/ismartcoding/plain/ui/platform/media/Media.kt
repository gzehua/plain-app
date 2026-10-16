package com.ismartcoding.plain.ui.platform.media

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.IntSize

expect fun tryDecodeQrCode(path: String): String?
expect fun generateQrCode(text: String, width: Int, height: Int): ImageBitmap
expect fun getSvgSize(path: String): IntSize
expect fun addMediaShortcut(path: String, label: String)
