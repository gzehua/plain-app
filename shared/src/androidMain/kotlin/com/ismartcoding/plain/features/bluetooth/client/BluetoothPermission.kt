package com.ismartcoding.plain.features.bluetooth.client

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.withResumed
import com.ismartcoding.plain.appContext
import com.ismartcoding.plain.i18n.Res
import com.ismartcoding.plain.i18n.bluetooth_scan_gps_enable_confirm
import com.ismartcoding.plain.i18n.bluetooth_scan_gps_enable_description
import com.ismartcoding.plain.i18n.bluetooth_scan_gps_enable_title
import com.ismartcoding.plain.i18n.cancel
import com.ismartcoding.plain.i18n.location_permission_should_be_enabled
import com.ismartcoding.plain.lib.extensions.hasPermission
import com.ismartcoding.plain.platform.isSPlus
import com.ismartcoding.plain.platform.network.BleAvailability
import com.ismartcoding.plain.platform.network.ble.client.AndroidBleScanner
import com.ismartcoding.plain.platform.network.refreshBleAvailability
import com.ismartcoding.plain.ui.components.DialogHelper
import com.ismartcoding.plain.ui.i18n.LocaleHelper
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class BluetoothPermission private constructor(private val activity: ComponentActivity) {
    private val requests = Mutex()
    private var pending: CompletableDeferred<Unit>? = null
    private var locationDialog: AlertDialog? = null
    private val enableBluetooth = activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { finishRequest() }
    private val requestLocation = activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { finishRequest() }
    private val requestScanConnect = activity.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { finishRequest() }
    private val locationSettings = activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { finishRequest() }

    private fun finishRequest() {
        refreshBleAvailability()
        val result = pending
        pending = null
        result?.complete(Unit)
    }

    private suspend fun awaitResult(launch: () -> Unit) {
        val result = CompletableDeferred<Unit>()
        pending = result
        try {
            launch()
        } catch (error: Exception) {
            pending = null
            throw error
        }
        try {
            result.await()
        } finally {
            locationDialog?.let {
                locationDialog = null
                it.dismiss()
                finishRequest()
            }
        }
    }

    private suspend fun requestAccess(): Boolean = requests.withLock {
        pending?.await()
        activity.lifecycle.withResumed { }
        if (isSPlus()) {
            val permissions = arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_ADVERTISE)
            if (!activity.hasPermission(*permissions)) {
                awaitResult { requestScanConnect.launch(permissions) }
                if (!activity.hasPermission(*permissions)) return@withLock false
            }
        }
        if (!AndroidBleScanner.getBluetoothAdapter().isEnabled) {
            awaitResult { enableBluetooth.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)) }
            if (!AndroidBleScanner.getBluetoothAdapter().isEnabled) return@withLock false
        }
        if (!isSPlus()) {
            if (!activity.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
                awaitResult { requestLocation.launch(Manifest.permission.ACCESS_FINE_LOCATION) }
                if (!activity.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    DialogHelper.showMessage(Res.string.location_permission_should_be_enabled)
                    return@withLock false
                }
            }
            if (needsLocationSettings()) {
                awaitResult {
                    locationDialog = AlertDialog.Builder(activity)
                        .setTitle(LocaleHelper.getString(Res.string.bluetooth_scan_gps_enable_title))
                        .setMessage(LocaleHelper.getString(Res.string.bluetooth_scan_gps_enable_description))
                        .setPositiveButton(LocaleHelper.getString(Res.string.bluetooth_scan_gps_enable_confirm)) { _, _ ->
                            locationDialog = null
                            try {
                                locationSettings.launch(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                            } catch (error: Exception) {
                                val result = pending
                                pending = null
                                result?.completeExceptionally(error)
                            }
                        }
                        .setNegativeButton(LocaleHelper.getString(Res.string.cancel)) { _, _ -> finishRequest() }
                        .setCancelable(false)
                        .show()
                }
                if (needsLocationSettings()) return@withLock false
            }
        }
        refreshBleAvailability() == BleAvailability.READY
    }

    companion object {
        private var current: BluetoothPermission? = null

        fun init(activity: ComponentActivity) {
            val permission = BluetoothPermission(activity)
            current = permission
            activity.lifecycle.addObserver(object : DefaultLifecycleObserver {
                override fun onDestroy(owner: LifecycleOwner) {
                    if (current === permission) current = null
                    permission.pending?.cancel()
                }
            })
        }

        private fun needsLocationSettings(): Boolean {
            if (isSPlus()) return false
            val location = appContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            return LocationManager.GPS_PROVIDER in location.allProviders && !location.isProviderEnabled(LocationManager.GPS_PROVIDER)
        }

        suspend fun ensurePermissionAsync(): Boolean = withContext(Dispatchers.Main.immediate) {
            if (refreshBleAvailability() == BleAvailability.READY && !needsLocationSettings()) return@withContext true
            val permission = current ?: error("Bluetooth permission requests require an active activity")
            permission.requestAccess()
        }
    }
}
