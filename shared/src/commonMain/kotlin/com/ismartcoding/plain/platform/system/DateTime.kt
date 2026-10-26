package com.ismartcoding.plain.platform.system

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

private val fileNameDateFormat = LocalDateTime.Format {
    year()
    monthNumber()
    day()
    char('_')
    hour()
    minute()
    second()
    char('_')
    secondFraction(3)
}

expect fun Instant.formatDateTime(): String

expect fun Instant.formatDate(): String

expect fun Instant.formatTime(): String

fun Instant.formatName(): String = toLocalDateTime(TimeZone.currentSystemDefault()).format(fileNameDateFormat)
