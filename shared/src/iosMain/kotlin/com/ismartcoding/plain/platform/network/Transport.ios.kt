package com.ismartcoding.plain.platform.network

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

actual val isWifiAwareSupported: Boolean = false
actual val wifiAwareAttached: StateFlow<Boolean> = MutableStateFlow(false)
internal actual val wifiAwareDiscoveredPeerCount: Int = 0
