package com.ismartcoding.plain.platform

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern(DiskLogFormatStrategy.LOG_DATE_FORMAT)

actual fun currentDateTimeString(): String = DATE_FORMATTER.format(LocalDateTime.now())
