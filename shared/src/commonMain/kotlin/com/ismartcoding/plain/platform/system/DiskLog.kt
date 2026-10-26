package com.ismartcoding.plain.platform.system

import com.ismartcoding.plain.lib.logcat.DiskLogAdapter
import com.ismartcoding.plain.lib.logcat.DiskLogStrategy
import com.ismartcoding.plain.lib.logcat.FormatStrategy
import com.ismartcoding.plain.lib.logcat.LogCat
import com.ismartcoding.plain.lib.logcat.LogStrategy
import com.ismartcoding.plain.rust.RustRuntime
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

private val logDateFormat = LocalDateTime.Format {
    year()
    char('-')
    monthNumber()
    char('-')
    day()
    char(' ')
    hour()
    char(':')
    minute()
    char(':')
    second()
    char('.')
    secondFraction(3)
}

class DiskLogFormatStrategy(private val logStrategy: LogStrategy) : FormatStrategy {
    override fun log(
        priority: Int,
        tag: String?,
        message: String,
    ) {
        val builder = StringBuilder()
        builder.append(currentDateTimeString())
        builder.append(SEPARATOR)
        builder.append(logLevel(priority))
        builder.append(SEPARATOR)
        builder.append(message)
        builder.append(NEW_LINE)
        logStrategy.log(priority, tag, builder.toString())
    }

    companion object {
        private const val NEW_LINE = "\n"
        private const val SEPARATOR = " "

        fun logLevel(value: Int): String = LogCat.logLevel(value)

        fun getInstance(): DiskLogFormatStrategy = DiskLogFormatStrategy(DiskLogStrategy())
    }
}

fun currentDateTimeString(): String = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).format(logDateFormat)

fun getLatestLogFilePath(): String = RustRuntime.logFile

fun initDiskLogging() {
    LogCat.logFilePath = getLatestLogFilePath()
    LogCat.addLogAdapter(
        DiskLogAdapter(
            DiskLogFormatStrategy.getInstance(),
            minPriority = if (isDebugBuild()) LogCat.VERBOSE else LogCat.WARN,
        ),
    )
}
