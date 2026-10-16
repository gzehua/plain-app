package com.ismartcoding.plain.features.session

import com.ismartcoding.plain.events.HConfirmToAcceptLoginEvent
import com.ismartcoding.plain.rust.request.RustContentApi
import com.ismartcoding.plain.rust.content.gql

object RustWebLogin {
    private suspend fun decide(requestId: String, allowed: Boolean) {
        RustContentApi.mutate("confirmWebLogin(requestId: ${gql(requestId)}, allowed: $allowed)")
    }
    suspend fun cancel(requestId: String) { decide(requestId, false) }
    suspend fun resetPassword(): String = com.ismartcoding.plain.preferences.RustSystemState.resetPassword()
    suspend fun complete(event: HConfirmToAcceptLoginEvent) { decide(event.requestId, true) }
}
