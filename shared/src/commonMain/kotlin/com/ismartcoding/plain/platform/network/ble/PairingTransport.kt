package com.ismartcoding.plain.platform.network.ble

import com.ismartcoding.plain.app.TempData
import com.ismartcoding.plain.platform.network.ble.server.BleGattServer
import com.ismartcoding.plain.platform.network.bleTransport
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch

/**
 * Owns the BLE GATT *server* used for pairing broadcasts and Nearby
 * notifications. Discovery scanning lives in
 * [com.ismartcoding.plain.features.nearby.BleScanDriver], whose session is owned by
 * Rust.
 */
object PairingTransport {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var server: BleGattServer? = null
    private var awareObserverJob: Job? = null

    fun startAdvertising() {
        if (server != null) return
        val s = bleTransport().createServer()
        s.start()
        server = s
        startAwareObserver()
    }

    fun stopAdvertising() {
        awareObserverJob?.cancel()
        awareObserverJob = null
        server?.stop()
        server = null
    }

    /** Whether the GATT server (advertising) is currently running. */
    fun isAdvertising(): Boolean = server != null

    fun refreshAdvertising() {
        server?.refreshAdvertising()
    }

    fun sendNotification(mac: String, charUuid: String, value: String): Boolean {
        return server?.sendNotification(mac, charUuid, value.encodeToByteArray()) ?: false
    }

    private fun startAwareObserver() {
        if (awareObserverJob?.isActive == true) return
        awareObserverJob = scope.launch {
            TempData.awareRunning.drop(1).collect {
                refreshAdvertising()
            }
        }
    }
}