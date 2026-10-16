package com.ismartcoding.plain.platform.network

actual fun startAwareIfNeeded(): Boolean = false
actual fun subscribeAwareForPeer(peer: AwarePeer, config: AwareConfig) {}
actual fun unsubscribeAwareForPeer(id: String) {}
actual fun stopAware() {}
