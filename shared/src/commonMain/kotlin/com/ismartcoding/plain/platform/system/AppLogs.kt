package com.ismartcoding.plain.platform.system

import com.ismartcoding.plain.graphql.Mutation
import com.ismartcoding.plain.graphql.Query
import com.ismartcoding.plain.rust.graphql.RustGraphqlApi
import com.ismartcoding.plain.i18n.Res
import com.ismartcoding.plain.i18n.no_logs_error
import com.ismartcoding.plain.lib.coMain
import com.ismartcoding.plain.ui.components.DialogHelper
import kotlinx.coroutines.CancellationException

suspend fun getLogFileSize(): Long =
    RustGraphqlApi.execute(Query.appLogFilesSize())

suspend fun clearLogsAsync() {
    RustGraphqlApi.execute(Mutation.clearAppLogs())
}

fun exportLogsAsync() {
    coMain {
        DialogHelper.showLoading()
        try {
            val path = RustGraphqlApi.execute(Mutation.exportAppLogs())
            if (path == null) {
                DialogHelper.showMessage(Res.string.no_logs_error)
            } else {
                shareLogArchive(path)
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
