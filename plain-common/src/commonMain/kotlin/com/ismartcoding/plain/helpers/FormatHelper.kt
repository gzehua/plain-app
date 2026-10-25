package com.ismartcoding.plain.helpers

import kotlin.math.pow
import kotlin.math.round
import kotlin.math.roundToLong

object FormatHelper {
    fun formatDouble(value: Double, digits: Int = 2, isGroupingUsed: Boolean = true): String {
        if (digits <= 0) {
            return round(value).roundToLong().toString()
        }
        val factor = 10.0.pow(digits)
        val rounded = round(value * factor) / factor
        val intPart = rounded.toLong()
        val fracPart = ((rounded - intPart) * factor).roundToLong()
        val fracStr = fracPart.toString().padStart(digits, '0').take(digits)
        return "$intPart.$fracStr"
    }

    fun formatFloat(value: Float, digits: Int = 2, isGroupingUsed: Boolean = true): String =
        formatDouble(value.toDouble(), digits, isGroupingUsed)
}