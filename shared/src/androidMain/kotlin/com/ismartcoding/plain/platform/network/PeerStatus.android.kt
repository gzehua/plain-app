package com.ismartcoding.plain.platform.network

import com.ismartcoding.plain.platform.isTPlus

import com.ismartcoding.plain.appContext
import com.ismartcoding.plain.features.chat.models.PeerRecord
import com.ismartcoding.plain.features.chat.peer.transport.WifiAwareTransport
import com.ismartcoding.plain.lib.extensions.hasPermission

actual fun startAwareIfNeeded(): Boolean {
    if (!isWifiAwareSupported) return false
    val hasPermission = if (isTPlus()) {
        appContext.hasPermission(android.Manifest.permission.NEARBY_WIFI_DEVICES)
    } else {
        appContext.hasPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
    }
    if (!hasPermission) return false
    WifiAwareTransport.start()
    return true
}

actual fun subscribeAwareForPeer(peer: PeerRecord) {
    if (!isWifiAwareSupported) return
    WifiAwareTransport.subscribe(peer)
}
