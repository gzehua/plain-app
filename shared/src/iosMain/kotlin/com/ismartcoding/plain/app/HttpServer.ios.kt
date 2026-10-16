package com.ismartcoding.plain.app


import com.ismartcoding.plain.preferences.*

import com.ismartcoding.plain.lib.coIO
import com.ismartcoding.plain.lib.withIO
import com.ismartcoding.plain.lib.logcat.LogCat
import com.ismartcoding.plain.app.HttpServerManager

actual suspend fun onHttpServerStarted() = Unit
actual suspend fun onHttpServerStopped() = Unit

/**
 * iOS entry: launch a coroutine running the shared [startHttpServerAsync]
 * orchestrator. There is no foreground-service requirement on iOS, so the
 * engine can be driven directly from a background coroutine.
 */
actual fun startHttpServerService() {
    coIO {
        LogCat.d("startHttpServer (iOS/Rust)")
        startHttpServerAsync()
    }
}

/** iOS has no foreground service; HTTP lifetime belongs to the app. */
actual suspend fun stopHttpServiceAsync(): Unit = withIO {
    HttpServerManager.backgroundState.value = com.ismartcoding.plain.features.settings.HttpServerState.OFF
}


actual fun getAwareAttachStatus(): String = "not available"
actual fun getAwareDiscoveredPeerCount(): Int = 0
