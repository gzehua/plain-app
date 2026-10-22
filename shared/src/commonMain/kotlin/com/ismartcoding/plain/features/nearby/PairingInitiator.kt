package com.ismartcoding.plain.features.nearby

import com.ismartcoding.plain.models.NearbyDevice
import com.ismartcoding.plain.lib.coIO
import com.ismartcoding.plain.lib.withIO
import kotlinx.coroutines.*

object PairingInitiator {

    suspend fun start(device: NearbyDevice) = withIO { RustPairingRuntime.start(device) }

    fun cancel(deviceId: String) { coIO { RustPairingRuntime.cancel(deviceId) } }
}
