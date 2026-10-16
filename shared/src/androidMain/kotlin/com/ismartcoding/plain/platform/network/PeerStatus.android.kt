package com.ismartcoding.plain.platform.network


import com.ismartcoding.plain.platform.network.AwarePeer
import com.ismartcoding.plain.platform.network.aware.WifiAwareTransport
import com.ismartcoding.plain.platform.permissions.isGranted

actual fun startAwareIfNeeded(): Boolean {
    val permission = wifiAwarePermission() ?: return false
    if (!permission.isGranted()) return false
    WifiAwareTransport.start()
    return true
}

actual fun subscribeAwareForPeer(peer: AwarePeer, config: AwareConfig) {
    if (!isWifiAwareSupported) return
    WifiAwareTransport.subscribe(peer, config)
}

actual fun unsubscribeAwareForPeer(id: String) {
    if (isWifiAwareSupported) WifiAwareTransport.unsubscribe(id)
}

actual fun stopAware() {
    if (isWifiAwareSupported) WifiAwareTransport.stop()
}
