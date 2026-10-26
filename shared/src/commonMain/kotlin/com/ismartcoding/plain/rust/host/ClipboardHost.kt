package com.ismartcoding.plain.rust.host

import com.ismartcoding.plain.events.EventType
import com.ismartcoding.plain.events.HostEvent
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.lib.logcat.LogCat
import com.ismartcoding.plain.platform.observeClipboardChanges
import com.ismartcoding.plain.Prefs
import com.ismartcoding.plain.rust.sendEventToRust
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

internal suspend fun observeClipboardReports() = observeClipboardWhenEnabled(
    Prefs.apiPermissions.map { "CLIPBOARD" in it },
) {
    observeClipboardChanges { report ->
        try {
            sendEventToRust(HostEvent(EventType.CLIPBOARD_REPORTED, JsonHelper.jsonEncode(report)))
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            LogCat.e("Clipboard report", error)
        }
    }
}

internal suspend fun observeClipboardWhenEnabled(enabled: Flow<Boolean>, observe: suspend () -> Unit) {
    enabled.distinctUntilChanged().collectLatest { if (it) observe() }
}
