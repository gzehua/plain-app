package com.ismartcoding.plain

import android.content.Context
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object CrashHandler {
    private const val CRASH_FILE_NAME = "crash_report.txt"
    private const val CRASH_LOG_NAME = "crash_log.txt"
    private lateinit var pendingReport: File
    private lateinit var crashHistory: File
    private val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    fun install(context: Context) {
        val directory = context.filesDir
        pendingReport = File(directory, CRASH_FILE_NAME)
        crashHistory = File(directory, CRASH_LOG_NAME)
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            saveCrash(throwable)
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }

    fun getPendingReport(): String? {
        val file = pendingReport
        if (!file.exists()) return null
        return try {
            val content = file.readText()
            file.delete()
            content
        } catch (_: Exception) {
            null
        }
    }

    private fun saveCrash(throwable: Throwable) {
        try {
            val sw = StringWriter()
            throwable.printStackTrace(PrintWriter(sw))
            val timestamp = dateFormat.format(LocalDateTime.now())
            val report = buildString {
                append("Time: $timestamp\n")
                append(sw.toString())
            }
            pendingReport.writeText(report)
            crashHistory.appendText(report + "\n\n")
        } catch (_: Exception) {}
    }
}
