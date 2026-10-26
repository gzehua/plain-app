package com.ismartcoding.plain.features.sessions

import com.ismartcoding.plain.rust.native.RustNativeApi

object RustWebLogin {
    private suspend fun decide(requestId: String, allowed: Boolean) {
        RustNativeApi.confirmWebLogin(requestId, allowed)
    }
    suspend fun cancel(requestId: String) { decide(requestId, false) }
    suspend fun complete(requestId: String) { decide(requestId, true) }
}
