package com.ismartcoding.plain.lib.qrcode

internal enum class QRCodeDataType(val value: Int) {

    NUMBERS(1 shl 0),

    UPPER_ALPHA_NUM(1 shl 1),

    DEFAULT(1 shl 2)
}
