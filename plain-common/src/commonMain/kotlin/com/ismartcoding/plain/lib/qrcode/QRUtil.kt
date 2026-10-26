package com.ismartcoding.plain.lib.qrcode

internal object QRUtil {
    fun getPatternPosition(typeNumber: Int): IntArray = PATTERN_POSITION_TABLE[typeNumber - 1]

    private val PATTERN_POSITION_TABLE = arrayOf(
        intArrayOf(),
        intArrayOf(6, 18),
        intArrayOf(6, 22),
        intArrayOf(6, 26),
        intArrayOf(6, 30),
        intArrayOf(6, 34),
        intArrayOf(6, 22, 38),
        intArrayOf(6, 24, 42),
        intArrayOf(6, 26, 46),
        intArrayOf(6, 28, 50),
        intArrayOf(6, 30, 54),
        intArrayOf(6, 32, 58),
        intArrayOf(6, 34, 62),
        intArrayOf(6, 26, 46, 66),
        intArrayOf(6, 26, 48, 70),
        intArrayOf(6, 26, 50, 74),
        intArrayOf(6, 30, 54, 78),
        intArrayOf(6, 30, 56, 82),
        intArrayOf(6, 30, 58, 86),
        intArrayOf(6, 34, 62, 90),
        intArrayOf(6, 28, 50, 72, 94),
        intArrayOf(6, 26, 50, 74, 98),
        intArrayOf(6, 30, 54, 78, 102),
        intArrayOf(6, 28, 54, 80, 106),
        intArrayOf(6, 32, 58, 84, 110),
        intArrayOf(6, 30, 58, 86, 114),
        intArrayOf(6, 34, 62, 90, 118),
        intArrayOf(6, 26, 50, 74, 98, 122),
        intArrayOf(6, 30, 54, 78, 102, 126),
        intArrayOf(6, 26, 52, 78, 104, 130),
        intArrayOf(6, 30, 56, 82, 108, 134),
        intArrayOf(6, 34, 60, 86, 112, 138),
        intArrayOf(6, 30, 58, 86, 114, 142),
        intArrayOf(6, 34, 62, 90, 118, 146),
        intArrayOf(6, 30, 54, 78, 102, 126, 150),
        intArrayOf(6, 24, 50, 76, 102, 128, 154),
        intArrayOf(6, 28, 54, 80, 106, 132, 158),
        intArrayOf(6, 32, 58, 84, 110, 136, 162),
        intArrayOf(6, 26, 54, 82, 110, 138, 166),
        intArrayOf(6, 30, 58, 86, 114, 142, 170)
    )

    fun getErrorCorrectPolynomial(errorCorrectLength: Int): Polynomial {
        var a = Polynomial(intArrayOf(1))
        for (i in 0 until errorCorrectLength) {
            a = a.multiply(Polynomial(intArrayOf(1, QRMath.gexp(i))))
        }
        return a
    }

    fun getDataType(s: String): QRCodeDataType = when {
        s.matches(Regex("[0-9]+")) -> QRCodeDataType.NUMBERS
        s.matches(Regex("[0-9A-Z $%*+\\-./:]+")) -> QRCodeDataType.UPPER_ALPHA_NUM
        else -> QRCodeDataType.DEFAULT
    }

    private const val G18 = (
        1 shl 12 or (1 shl 11) or (1 shl 10)
            or (1 shl 9) or (1 shl 8) or (1 shl 5) or (1 shl 2) or (1 shl 0)
        )
    fun getBCHTypeNumber(data: Int): Int {
        var d = data shl 12
        while (getBCHDigit(d) - getBCHDigit(G18) >= 0) {
            d = d xor (G18 shl getBCHDigit(d) - getBCHDigit(G18))
        }
        return data shl 12 or d
    }

    private fun getBCHDigit(data: Int): Int {
        var i = data
        var digit = 0
        while (i != 0) {
            digit++
            i = i ushr 1
        }
        return digit
    }
}
