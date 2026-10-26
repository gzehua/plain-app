package com.ismartcoding.plain.platform.network

import com.ismartcoding.plain.Prefs
import com.ismartcoding.plain.platform.network.ble.BleUuids
import com.ismartcoding.plain.platform.network.ble.PairingTransport

actual fun getBleDebugInfo(): BleDebugInfo {
    // iOS exposes Bluetooth support/enabled as always available (see
    // Bluetooth.ios.kt). Permission granularity on iOS is handled via
    // CBCentralManager state rather than discrete permission grants, so the
    // individual permission flags stay false here — matching the existing
    // isBleReady() stub. The advertisingRunning / clientId / serviceUuid
    // fields come from the common PairingTransport and are real values.
    return BleDebugInfo(
        bluetoothSupported = isBluetoothSupported(),
        bluetoothEnabled = isBluetoothEnabled(),
        advertisingRunning = PairingTransport.isAdvertising(),
        clientId = Prefs.clientId.value,
        serviceUuid = BleUuids.SERVICE_UUID,
    )
}
