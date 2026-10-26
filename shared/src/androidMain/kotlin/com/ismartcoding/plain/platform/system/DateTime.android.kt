package com.ismartcoding.plain.platform.system

import com.ismartcoding.plain.appContext
import com.ismartcoding.plain.ui.i18n.LocaleHelper
import java.text.DateFormat
import java.util.Calendar
import kotlin.time.Instant
import android.text.format.DateFormat as AndroidDateFormat
import java.util.Locale as JavaLocale

actual fun Instant.formatTime(): String {
    val c = Calendar.getInstance()
    c.timeInMillis = epochSeconds * 1000
    return AndroidDateFormat.getTimeFormat(appContext)
        .format(c.time)
}

actual fun Instant.formatDateTime(): String {
    val c = Calendar.getInstance()
    c.timeInMillis = epochSeconds * 1000
    val l = LocaleHelper.currentLocale()
    return DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, JavaLocale(l.language, l.country))
        .format(c.time)
}

actual fun Instant.formatDate(): String {
    val c = Calendar.getInstance()
    c.timeInMillis = epochSeconds * 1000
    val l = LocaleHelper.currentLocale()
    return DateFormat.getDateInstance(DateFormat.MEDIUM, JavaLocale(l.language, l.country))
        .format(c.time)
}
