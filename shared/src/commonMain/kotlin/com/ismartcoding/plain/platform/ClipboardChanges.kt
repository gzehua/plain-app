package com.ismartcoding.plain.platform

import com.ismartcoding.plain.events.ClipboardReportData
import kotlinx.coroutines.flow.Flow

internal expect suspend fun observeClipboardChanges(report: suspend (ClipboardReportData) -> Unit)

internal suspend fun forwardClipboardChanges(
    changes: Flow<Unit>,
    active: () -> Boolean,
    revision: () -> Long,
    read: () -> String?,
    report: suspend (ClipboardReportData) -> Unit,
) {
    var lastRevision: Long? = null
    changes.collect {
        if (!active()) return@collect
        val current = revision()
        if (current == lastRevision) return@collect
        lastRevision = current
        val text = read() ?: return@collect
        if (text.isNotBlank() && text.length <= 256 * 1024) {
            report(ClipboardReportData(text, sensitive = false))
        }
    }
}
