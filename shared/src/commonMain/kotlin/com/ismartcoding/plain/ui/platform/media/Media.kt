package com.ismartcoding.plain.ui.platform.media

import androidx.compose.ui.unit.IntSize

expect fun tryDecodeQrCode(path: String): String?
expect fun getSvgSize(path: String): IntSize
