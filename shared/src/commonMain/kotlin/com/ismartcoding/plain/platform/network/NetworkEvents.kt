package com.ismartcoding.plain.platform.network

import com.ismartcoding.plain.app.events.EventType
import com.ismartcoding.plain.app.events.HostEvent
import com.ismartcoding.plain.lib.coIO
import com.ismartcoding.plain.lib.logcat.LogCat
import com.ismartcoding.plain.rust.PlainRust
import com.ismartcoding.plain.rust.request.RustRuntime
import kotlinx.coroutines.CancellationException

object NetworkEvents {
    fun changed() {
        if (RustRuntime.startedSession() == null) return
        coIO {
            try { PlainRust.publish(HostEvent(EventType.NETWORK_INTERFACES_CHANGED, "null")) }
            catch (cancelled: CancellationException) { throw cancelled }
            catch (error: Exception) { LogCat.e("Network change report failed", error) }
        }
    }
}
