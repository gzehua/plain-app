package com.ismartcoding.plain.rust.host.network

import com.ismartcoding.plain.lib.JsonHelper
import kotlinx.serialization.Serializable

import com.ismartcoding.plain.platform.network.bleTransport
import kotlinx.serialization.json.*

object NearbyScanHost {
    fun facts(): JsonElement = JsonHelper.jsonEncodeToElement(ScanFacts(bleTransport().createScanner().isScanPaused()))
}

@Serializable
private data class ScanFacts(val paused: Boolean)
