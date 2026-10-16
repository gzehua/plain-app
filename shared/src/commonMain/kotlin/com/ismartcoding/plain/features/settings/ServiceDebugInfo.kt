package com.ismartcoding.plain.features.settings

import com.ismartcoding.plain.app.TempData
import com.ismartcoding.plain.platform.network.ble.BleUuids
import com.ismartcoding.plain.platform.network.ble.PairingTransport
import com.ismartcoding.plain.features.dlna.DlnaRendererState
import com.ismartcoding.plain.app.HttpServerManager
import com.ismartcoding.plain.features.sessions.onlineClientIds
import com.ismartcoding.plain.app.getAwareAttachStatus
import com.ismartcoding.plain.app.getAwareDiscoveredPeerCount
import com.ismartcoding.plain.app.isHttpServerRunning
import com.ismartcoding.plain.app.isMdnsRunning
import com.ismartcoding.plain.preferences.UserPrefs

data class ServiceDebugInfo(
    val httpServerRunning: Boolean = false,
    val httpServerState: String = "",
    val httpPort: Int = 0,
    val httpsPort: Int = 0,
    val wsSessionCount: Int = 0,
    val httpServerError: String = "",

    val mdnsRunning: Boolean = false,
    val mdnsHostname: String = "",

    val dlnaRunning: Boolean = false,
    val dlnaPlaybackState: String = "",
    val dlnaStartError: String = "",

    val bleRunning: Boolean = false,
    val bleClientId: String = "",
    val bleServiceUuid: String = "",

    val awareRunning: Boolean = false,
    val awareAttachStatus: String = "",
    val awareDiscoveredPeerCount: Int = 0,
)

fun getServiceDebugInfo(): ServiceDebugInfo {
    val httpRunning = isHttpServerRunning()
    val dlnaRunning = DlnaRendererState.isRunning.value
    val bleRunning = PairingTransport.isAdvertising()
    val awareRunning = TempData.awareRunning.value

    return ServiceDebugInfo(
        httpServerRunning = httpRunning,
        httpServerState = if (httpRunning) "ON" else "OFF",
        httpPort = UserPrefs.httpPort.value,
        httpsPort = UserPrefs.httpsPort.value,
        wsSessionCount = onlineClientIds.value.size,
        httpServerError = HttpServerManager.httpServerError.value,

        mdnsRunning = isMdnsRunning(),
        mdnsHostname = TempData.mdnsHostname,

        dlnaRunning = dlnaRunning,
        dlnaPlaybackState = DlnaRendererState.playbackState.value.name,
        dlnaStartError = DlnaRendererState.startError.value,

        bleRunning = bleRunning,
        bleClientId = TempData.clientId,
        bleServiceUuid = BleUuids.SERVICE_UUID,

        awareRunning = awareRunning,
        awareAttachStatus = getAwareAttachStatus(),
        awareDiscoveredPeerCount = getAwareDiscoveredPeerCount(),
    )
}
