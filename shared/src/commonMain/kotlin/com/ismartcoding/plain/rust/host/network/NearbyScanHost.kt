package com.ismartcoding.plain.rust.host.network

import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.platform.network.bleTransport
import com.ismartcoding.plain.rust.host.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

object NearbyScanHost {
    internal fun facts(): HostResult = hostResult(ScanFacts(bleTransport().createScanner().isScanPaused()))
}

@Serializable
private data class ScanFacts(val paused: Boolean)
