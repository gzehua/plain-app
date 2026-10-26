package com.ismartcoding.plain.ui.platform.media

import android.graphics.BitmapFactory
import androidx.compose.ui.unit.IntSize
import com.ismartcoding.plain.ui.media.SvgHelper
import com.ismartcoding.plain.ui.scanner.QrCodeScanHelper

actual fun tryDecodeQrCode(path: String): String? {
    return try {
        val bitmap = BitmapFactory.decodeFile(path) ?: return null
        QrCodeScanHelper.tryDecode(bitmap)
    } catch (e: Exception) {
        null
    }
}

actual fun getSvgSize(path: String): IntSize = SvgHelper.getSize(path)
