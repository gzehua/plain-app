package com.ismartcoding.plain.platform.network

import com.ismartcoding.plain.platform.system.IosBluetoothMonitor

import com.ismartcoding.plain.platform.network.ble.client.BleScanner
import com.ismartcoding.plain.platform.network.ble.client.IosBleScanner
import com.ismartcoding.plain.platform.network.ble.server.BleGattServer
import com.ismartcoding.plain.platform.network.ble.server.IosBleGattServer
import kotlinx.coroutines.flow.StateFlow

actual fun bleAvailability(): BleAvailability = IosBluetoothMonitor.availability.value

actual val bleAvailabilityFlow: StateFlow<BleAvailability> = IosBluetoothMonitor.availability

/**
 * Creating the central manager for the first time shows the one-time system
 * Bluetooth prompt; the returned state resolves once the user answers.
 */
actual suspend fun ensureBlePermissionAsync(): Boolean = IosBleScanner.ensurePermission()

actual fun setBluetoothCanContinue(value: Boolean) {}

actual fun bleTransport(): BleTransport = IosBleTransport

object IosBleTransport : BleTransport {
    override fun createScanner(): BleScanner = IosBleScanner

    override fun createServer(): BleGattServer = IosBleGattServer()
}

actual fun canRequestBluetoothAccess(): Boolean = bleAvailability() == BleAvailability.UNKNOWN
