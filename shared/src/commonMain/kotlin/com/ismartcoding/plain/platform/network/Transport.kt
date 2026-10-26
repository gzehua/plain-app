package com.ismartcoding.plain.platform.network

import kotlinx.coroutines.flow.StateFlow

expect val isWifiAwareSupported: Boolean
expect val wifiAwareAttached: StateFlow<Boolean>
internal expect val wifiAwareDiscoveredPeerCount: Int
