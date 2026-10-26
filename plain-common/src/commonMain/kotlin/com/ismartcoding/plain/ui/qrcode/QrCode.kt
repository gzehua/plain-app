package com.ismartcoding.plain.ui.qrcode

import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import com.ismartcoding.plain.lib.qrcode.QRCode

fun generateQrCode(text: String, width: Int, height: Int): ImageBitmap {
    val matrix = QRCode(text).encode()
    val moduleCount = matrix.size
    val quietZone = 4
    val scale = minOf(width, height) / (moduleCount + quietZone * 2)
    require(scale > 0) { "QR code does not fit the requested image size" }
    val image = ImageBitmap(width, height)
    val canvas = Canvas(image)
    val paint = Paint().apply {
        isAntiAlias = false
        color = Color.White
    }
    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
    paint.color = Color.Black
    val left = (width - moduleCount * scale) / 2
    val top = (height - moduleCount * scale) / 2
    for (y in matrix.indices) {
        for (x in matrix[y].indices) {
            if (matrix[y][x]) {
                val x0 = left + x * scale
                val y0 = top + y * scale
                canvas.drawRect(x0.toFloat(), y0.toFloat(), (x0 + scale).toFloat(), (y0 + scale).toFloat(), paint)
            }
        }
    }
    return image
}
