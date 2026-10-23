package com.ismartcoding.plain.ui.nearby

import com.ismartcoding.plain.features.nearby.RustNearbyDevices

import com.ismartcoding.plain.ui.state.launchSafe

import com.ismartcoding.plain.platform.network.ble.PairingTransport

import com.ismartcoding.plain.models.NearbyDevice
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import com.ismartcoding.plain.features.chat.peer.PeerManager
import com.ismartcoding.plain.features.nearby.PairingInitiator
import com.ismartcoding.plain.lib.logcat.LogCat
import com.ismartcoding.plain.platform.network.ensureBlePermissionAsync
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.json.JsonObject

object NearbyViewModel {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val nearbyDevices = MutableStateFlow<List<NearbyDevice>>(emptyList())
    var isDiscovering = mutableStateOf(false)
    val pairingStates = mutableStateMapOf<String, JsonObject>()
    val itemStatus = mutableStateMapOf<String, NearbyItemStatus>()


    private var blePermissionJob: Job? = null

    fun requestBlePermission() {
        if (blePermissionJob?.isActive == true) return
        blePermissionJob = scope.launchSafe(
            onError = {
                LogCat.e("BLE permission error: ${it.message}", it)
            },
            block = {
                ensureBlePermissionAsync()
            }
        )
    }

    fun startPairing(device: NearbyDevice) {
        if (itemStatus[device.id] != null) return
        itemStatus[device.id] = NearbyItemStatus.STARTING

        scope.launchSafe { PairingInitiator.start(device) }
    }

    fun unpairDevice(deviceId: String) {
        val current = itemStatus[deviceId]
        if (current == NearbyItemStatus.UNPAIRING || current == NearbyItemStatus.PAIRING) return
        itemStatus[deviceId] = NearbyItemStatus.UNPAIRING
        scope.launchSafe(onDone = {
            itemStatus.remove(deviceId)
        }) {
            PeerManager.markUnpaired(deviceId)
        }
    }

    fun cancelPairing(deviceId: String) {
        itemStatus.remove(deviceId)
        PairingInitiator.cancel(deviceId)
    }

    fun getStatus(deviceId: String, isPaired: Boolean): NearbyItemStatus {
        return when (itemStatus[deviceId]) {
            NearbyItemStatus.UNPAIRING -> NearbyItemStatus.UNPAIRING
            NearbyItemStatus.STARTING -> NearbyItemStatus.STARTING
            NearbyItemStatus.PAIRING -> if (isPaired) NearbyItemStatus.PAIRED else NearbyItemStatus.PAIRING
            else -> if (isPaired) NearbyItemStatus.PAIRED else NearbyItemStatus.UNPAIRED
        }
    }

    fun handlePairingSuccess(deviceId: String) {
        itemStatus[deviceId] = NearbyItemStatus.PAIRED
    }
}
