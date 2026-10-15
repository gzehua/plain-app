package com.ismartcoding.plain.discover

import com.ismartcoding.plain.ble.client.BleGattClient
import com.ismartcoding.plain.data.DNearbyDevice
import com.ismartcoding.plain.data.DPairingCancel
import com.ismartcoding.plain.data.DPairingRequest
import com.ismartcoding.plain.data.DPairingResponse
import com.ismartcoding.plain.enums.NearbyMessageType
import com.ismartcoding.plain.enums.DiscoveryMethod
import com.ismartcoding.plain.events.HPairingSuccessEvent
import com.ismartcoding.plain.lib.TimeHelper
import com.ismartcoding.plain.lib.sendEvent
import com.ismartcoding.plain.ui.models.NearbyViewModel

object PairingCore {

    // ---- Discovery ----------------------------------------------------------

    suspend fun formatMessage(type: NearbyMessageType, json: String): String = RustNearbyWire.encode(type, json)

    fun replyToDevice(reply: DDiscoverReply, bleClient: BleGattClient? = null): DNearbyDevice {
        return DNearbyDevice(
            id = reply.id,
            name = reply.name,
            ips = reply.ips,
            port = reply.port,
            deviceType = reply.deviceType,
            version = reply.version,
            platform = reply.platform,
            lastSeen = TimeHelper.now(),
            discoveryMethods = if (bleClient == null) setOf(DiscoveryMethod.LAN) else setOf(DiscoveryMethod.BLE),
            bleClient = bleClient,
        )
    }

    // ---- Initiator (send request / receive response) -----------------------

    suspend fun handlePairResponse(response: DPairingResponse) {
        RustPairingRuntime.complete(response)
    }

    suspend fun handlePairRequest(request: DPairingRequest, senderAddress: String) {
        RustPairingRuntime.receiveRequest(request, senderAddress)
    }

    suspend fun handlePairCancel(cancel: DPairingCancel) { RustPairingRuntime.receiveCancel(cancel) }

    internal suspend fun publishSuccess(id: String, name: String, ip: String, key: String) {
        com.ismartcoding.plain.chat.peer.PeerManager.load()
        NearbyViewModel.handlePairingSuccess(id)
        sendEvent(HPairingSuccessEvent(id, name, ip, key))
    }

}
