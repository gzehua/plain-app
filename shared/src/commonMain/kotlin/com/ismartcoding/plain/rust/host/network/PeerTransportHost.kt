package com.ismartcoding.plain.rust.host.network

import com.ismartcoding.plain.features.chat.peer.transport.PeerTransportType
import com.ismartcoding.plain.platform.network.ble.PeerBleExchange
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.platform.network.isWifiAwareSupported
import com.ismartcoding.plain.platform.network.isBleReady
import kotlinx.serialization.json.*

object PeerTransportHost {
    suspend fun handle(method: String, params: JsonObject): JsonElement {
        if (method.startsWith("peerTransportPrewarm")) return PeerTransportPrewarmHost.handle(method, params)
        if (method == "peerTransportCapabilities") return JsonHelper.jsonEncodeToElement(buildList {
            if (isWifiAwareSupported) add(PeerTransportType.AWARE)
            if (isBleReady()) add(PeerTransportType.BLE)
        })
        if (method.startsWith("peerTransportSocket")) return PeerSocketHost.handle(method, params)
        if (method == "peerTransportBleExchange") return PeerBleExchange.exchange(params)
        error("Unknown peer SDK operation")
    }
}
