package com.ismartcoding.plain.platform.system


import com.ismartcoding.plain.network.HttpLogSink
import com.ismartcoding.plain.network.httpLogSink
import com.ismartcoding.plain.lib.logcat.DiskLogAdapter
import com.ismartcoding.plain.lib.logcat.DiskLogStrategy
import com.ismartcoding.plain.lib.logcat.FormatStrategy
import com.ismartcoding.plain.lib.logcat.LogCat
import com.ismartcoding.plain.lib.logcat.LogStrategy
import com.ismartcoding.plain.rust.RustRuntime

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
        internal const val LOG_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS"
        private const val NEW_LINE = "\n"
        private const val SEPARATOR = " "

        fun logLevel(value: Int): String = LogCat.logLevel(value)

        fun getInstance(): DiskLogFormatStrategy = DiskLogFormatStrategy(DiskLogStrategy())
    }
}

expect fun currentDateTimeString(): String

/**
 * Absolute path to the latest log file (e.g. `<logFolder>/latest.log`).
 */
fun getLatestLogFilePath(): String = RustRuntime.logFile

/**
 * Initialize disk logging and HTTP request logging.
 * - Debug builds: all log levels (VERBOSE+) are written to disk
 * - Release builds: only WARN+ are written to disk
 * Idempotent — safe to call multiple times (LogCat.addLogAdapter just appends).
 */
fun initDiskLogging() {
    LogCat.logFilePath = getLatestLogFilePath()
    LogCat.addLogAdapter(
        DiskLogAdapter(
            DiskLogFormatStrategy.getInstance(),
            minPriority = if (isDebugBuild()) LogCat.VERBOSE else LogCat.WARN,
        ),
    )
    httpLogSink = HttpLogSink { LogCat.v(it) }
    // Self-test: guarantee at least one line is written so the log file is
    // never empty. A WARN always passes the adapter's minPriority filter.
    LogCat.w("DiskLog: initialized (debug=${isDebugBuild()})")
}
