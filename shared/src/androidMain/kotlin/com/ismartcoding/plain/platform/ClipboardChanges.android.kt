package com.ismartcoding.plain.platform

import com.ismartcoding.plain.app.events.ClipboardReportData
import com.ismartcoding.plain.features.ClipboardWatcher
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.coroutineScope

internal actual suspend fun observeClipboardChanges(report: suspend (ClipboardReportData) -> Unit): Unit = coroutineScope {
    try {
        ClipboardWatcher.start(this, report)
        awaitCancellation()
    } finally {
        ClipboardWatcher.stop()
    }
}
