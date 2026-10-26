package com.ismartcoding.plain.platform.network


import com.ismartcoding.plain.platform.network.aware.WifiAwareTransport
import com.ismartcoding.plain.platform.permissions.isGranted

fun startAwareIfNeeded(): Boolean {
    val permission = wifiAwarePermission() ?: return false
    if (!permission.isGranted()) return false
    WifiAwareTransport.start()
    return true
}

fun subscribeAwareForPeer(peer: AwarePeer, config: AwareConfig) {
    WifiAwareTransport.subscribe(peer, config)
}

fun unsubscribeAwareForPeer(id: String) {
    WifiAwareTransport.unsubscribe(id)
}

fun stopAware() {
    WifiAwareTransport.stop()
}
