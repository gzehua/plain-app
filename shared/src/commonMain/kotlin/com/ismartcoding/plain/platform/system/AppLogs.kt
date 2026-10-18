package com.ismartcoding.plain.platform.system

import com.ismartcoding.plain.i18n.Res
import com.ismartcoding.plain.i18n.no_logs_error
import com.ismartcoding.plain.lib.coMain
import com.ismartcoding.plain.rust.PlainRust
import com.ismartcoding.plain.ui.components.DialogHelper
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long

suspend fun getLogFileSize(): Long =
    PlainRust.mainQuery("appLogFilesSize").getValue("appLogFilesSize").jsonPrimitive.long

suspend fun clearLogsAsync() {
    PlainRust.mainMutate("clearAppLogs")
}

fun exportLogsAsync() {
    coMain {
        DialogHelper.showLoading()
        try {
            val path = PlainRust.mainMutate("exportAppLogs").getValue("exportAppLogs")
            if (path is JsonNull) {
                DialogHelper.showMessage(Res.string.no_logs_error)
            } else {
                shareLogArchive(path.jsonPrimitive.content)
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            DialogHelper.showErrorMessage(error.toString())
        } finally {
            DialogHelper.hideLoading()
        }
    }
}

internal expect fun shareLogArchive(path: String)
