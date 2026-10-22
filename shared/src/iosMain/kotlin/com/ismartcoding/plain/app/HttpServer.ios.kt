package com.ismartcoding.plain.app

actual suspend fun onHttpServerStarted() = Unit
actual suspend fun onHttpServerStopped() = Unit

// iOS has no foreground-service retention; RustRuntime owns core startup.
actual fun startHttpServerService() = Unit
actual suspend fun stopHttpServiceAsync() = Unit

actual fun getAwareAttachStatus(): String = "not available"
actual fun getAwareDiscoveredPeerCount(): Int = 0
