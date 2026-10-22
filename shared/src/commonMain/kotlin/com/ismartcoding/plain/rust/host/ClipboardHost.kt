package com.ismartcoding.plain.rust.host

import com.ismartcoding.plain.rust.RustEventHandler

import com.ismartcoding.plain.app.events.EventType
import com.ismartcoding.plain.app.events.HostEvent
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.lib.logcat.LogCat
import com.ismartcoding.plain.platform.observeClipboardChanges
import com.ismartcoding.plain.preferences.RustSystemState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

internal suspend fun observeClipboardReports() = observeClipboardWhenEnabled(
    RustSystemState.state.map { "CLIPBOARD" in it.apiPermissions },
) {
    observeClipboardChanges { report ->
        try {
            RustEventHandler.sendEvent(HostEvent(EventType.CLIPBOARD_REPORTED, JsonHelper.jsonEncode(report)))
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
