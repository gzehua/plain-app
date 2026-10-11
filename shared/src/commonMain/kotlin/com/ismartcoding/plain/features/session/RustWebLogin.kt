package com.ismartcoding.plain.features.session

import com.ismartcoding.plain.events.HConfirmToAcceptLoginEvent
import com.ismartcoding.plain.rust.bridge.RustDispatchers
import com.ismartcoding.plain.rust.bridge.RustWebLoginBridge
import kotlinx.coroutines.withContext

object RustWebLogin {
    private suspend fun decide(requestId: String, allowed: Boolean) = withContext(RustDispatchers.requests) {
        val error = RustWebLoginBridge.decide(requestId, allowed)
        check(error.isEmpty()) { error }
    }
    suspend fun cancel(requestId: String) { decide(requestId, false) }
    suspend fun resetPassword(): String = com.ismartcoding.plain.preferences.RustSystemState.resetPassword()
    suspend fun complete(event: HConfirmToAcceptLoginEvent) { decide(event.requestId, true) }
}
