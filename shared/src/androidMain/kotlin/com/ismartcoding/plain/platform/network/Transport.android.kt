package com.ismartcoding.plain.platform.network

import com.ismartcoding.plain.platform.isTPlus

import com.ismartcoding.plain.platform.network.aware.WifiAwareTransport
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

actual val isWifiAwareSupported: Boolean
    get() = isTPlus() && WifiAwareTransport.isSupported()

actual val wifiAwareAttached: StateFlow<Boolean> by lazy {
    if (isTPlus()) WifiAwareTransport.attached else MutableStateFlow(false)
}

internal actual val wifiAwareDiscoveredPeerCount: Int
    get() = if (isWifiAwareSupported) WifiAwareTransport.discoveredPeerCount else 0
