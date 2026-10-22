package com.ismartcoding.plain.features.sessions

import com.ismartcoding.plain.rust.native.RustNativeApi


import com.ismartcoding.plain.app.events.HConfirmToAcceptLoginEvent

object RustWebLogin {
    private suspend fun decide(requestId: String, allowed: Boolean) {
        RustNativeApi.confirmWebLogin(requestId, allowed)
    }
    suspend fun cancel(requestId: String) { decide(requestId, false) }
    suspend fun resetPassword(): String = com.ismartcoding.plain.preferences.RustSystemState.resetPassword()
    suspend fun complete(event: HConfirmToAcceptLoginEvent) { decide(event.requestId, true) }
}
