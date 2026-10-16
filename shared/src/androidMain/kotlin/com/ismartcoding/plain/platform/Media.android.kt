package com.ismartcoding.plain.platform

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.IntSize
import com.ismartcoding.plain.appContext
import com.ismartcoding.plain.helpers.MediaShortcutHelper
import com.ismartcoding.plain.helpers.QrCodeGenerateHelper
import com.ismartcoding.plain.helpers.SvgHelper
import com.ismartcoding.plain.ui.scanner.QrCodeScanHelper
import java.io.File

actual fun tryDecodeQrCode(path: String): String? {
    return try {
        val bitmap = BitmapFactory.decodeFile(path) ?: return null
        QrCodeScanHelper.tryDecode(bitmap)
    } catch (e: Exception) {
        null
    }
}

actual fun fileLength(path: String): Long {
    return runCatching { File(path).takeIf { it.exists() }?.length() ?: 0L }.getOrDefault(0L)
}


actual fun generateQrCode(text: String, width: Int, height: Int): ImageBitmap {
    return QrCodeGenerateHelper.generate(text, width, height).asImageBitmap()
}

actual fun getSvgSize(path: String): IntSize = SvgHelper.getSize(path)

actual fun addMediaShortcut(path: String, label: String) {
    MediaShortcutHelper.addToDesktop(appContext, path, label, appResourceMipmap("ic_launcher"))
}
