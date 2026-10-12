package com.ismartcoding.plain.ui.models

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import com.ismartcoding.plain.ble.PairingTransport
import com.ismartcoding.plain.chat.peer.PeerManager
import com.ismartcoding.plain.data.DNearbyDevice
import com.ismartcoding.plain.discover.MdnsDiscoverManager
import com.ismartcoding.plain.discover.PairingInitiator
import com.ismartcoding.plain.lib.logcat.LogCat
import com.ismartcoding.plain.platform.ensureBlePermissionAsync
import com.ismartcoding.plain.platform.isBluetoothReadyToUse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow

object NearbyViewModel {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val nearbyDevices = MutableStateFlow<List<DNearbyDevice>>(emptyList())
    var isDiscovering = mutableStateOf(false)
    val itemStatus = mutableStateMapOf<String, NearbyItemStatus>()

    var isBleScanning = mutableStateOf(false)

    private var blePermissionJob: Job? = null

    fun startDiscovering() {
        MdnsDiscoverManager.startPeriodicDiscovery()
        scope.launchSafe {
            val devices = com.ismartcoding.plain.discover.RustNearbyDevices.startDiscovery()
            com.ismartcoding.plain.discover.RustNearbyDevices.apply(devices)
        }
    }

    fun stopDiscovering() {
        MdnsDiscoverManager.stopPeriodicDiscovery()
        scope.launchSafe { com.ismartcoding.plain.discover.RustNearbyDevices.stopDiscovery() }
    }

    fun requestBlePermission() {
        if (blePermissionJob?.isActive == true) return
        blePermissionJob = scope.launchSafe(
            onError = {
                LogCat.e("BLE permission error: ${it.message}", it)
            },
            block = {
                if (ensureBlePermissionAsync()) {
                    startBleScanning()
                }
            }
        )
    }

    fun startBleScanning() {
        if (isBleScanning.value) return
        if (!isBluetoothReadyToUse()) return
        isBleScanning.value = true
        // The App only runs the BLE radio loop; Rust owns the session, drives
        // GATT reads, and publishes discovered devices over the event stream.
        com.ismartcoding.plain.discover.BleScanDriver.start()
    }

    fun stopBleScanning() {
        isBleScanning.value = false
        com.ismartcoding.plain.discover.BleScanDriver.stop()
    }

    fun startPairing(device: DNearbyDevice) {
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
