@file:OptIn(ExperimentalForeignApi::class)

package com.ismartcoding.plain.ui.platform.media


import androidx.compose.ui.unit.IntSize
import com.ismartcoding.plain.lib.logcat.LogCat
import com.ismartcoding.plain.lib.toByteArray
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSFileManager
import platform.UIKit.UIImage
import platform.darwin.NSObject
import platform.objc.sel_registerName
import platform.CoreImage.CIContext
import platform.CoreImage.CIDetector
import platform.CoreImage.CIDetectorTypeQRCode
import platform.CoreImage.CIImage as CoreImageCIImage

actual fun tryDecodeQrCode(path: String): String? = try {
    val image = UIImage.imageWithContentsOfFile(path) ?: return null
    // file-backed UIImages have no CIImage; bridge through CGImage instead
    val ciImage = image.CIImage() ?: image.CGImage?.let { CoreImageCIImage.imageWithCGImage(it) } ?: return null
    val context = CIContext.context()
    val detector = CIDetector.detectorOfType(
        CIDetectorTypeQRCode, context, null,
    ) ?: return null
    val features = detector.featuresInImage(ciImage) ?: return null
    for (raw in features) {
        val feature = raw as? NSObject ?: continue
        val msg = feature.performSelector(sel_registerName("messageString")) as? String
        if (!msg.isNullOrEmpty()) return msg
    }
    null
} catch (e: Exception) {
    LogCat.e("tryDecodeQrCode: ${e.message}")
    null
}

actual fun getSvgSize(path: String): IntSize = parseSvgSize(path) ?: IntSize(150, 150)

private fun parseSvgSize(path: String): IntSize? {
    return try {
        val nsData = NSFileManager.defaultManager.contentsAtPath(path) ?: return null
        val text = nsData.toByteArray().decodeToString()
        val wMatch = Regex("""<svg[^>]*\bwidth\s*=\s*"?([0-9.]+)""", RegexOption.IGNORE_CASE).find(text)
        val hMatch = Regex("""<svg[^>]*\bheight\s*=\s*"?([0-9.]+)""", RegexOption.IGNORE_CASE).find(text)
        val w = wMatch?.groupValues?.get(1)?.toIntOrNull()
        val h = hMatch?.groupValues?.get(1)?.toIntOrNull()
        if (w != null && h != null) IntSize(w, h) else null
    } catch (e: Exception) {
        null
    }
}
