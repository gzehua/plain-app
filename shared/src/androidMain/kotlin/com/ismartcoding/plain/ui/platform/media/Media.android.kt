package com.ismartcoding.plain.ui.platform.media

import com.ismartcoding.plain.platform.system.appResourceMipmap

import android.graphics.BitmapFactory
import androidx.compose.ui.unit.IntSize
import com.ismartcoding.plain.appContext
import com.ismartcoding.plain.platform.system.MediaShortcutHelper
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

fun addMediaShortcut(path: String, label: String) {
    MediaShortcutHelper.addToDesktop(appContext, path, label, appResourceMipmap("ic_launcher"))
}
