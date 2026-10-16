package com.ismartcoding.plain.ui.platform.files

import androidx.compose.ui.text.TextMeasurer
import com.ismartcoding.plain.appContext
import com.ismartcoding.plain.ui.platform.files.PrintHelper

actual fun printText(textMeasurer: TextMeasurer, jobName: String, content: String) {
    PrintHelper.printText(appContext, textMeasurer, jobName, content)
}
