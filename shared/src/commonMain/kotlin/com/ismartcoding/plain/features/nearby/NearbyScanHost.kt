package com.ismartcoding.plain.features.nearby

import com.ismartcoding.plain.platform.network.bleTransport
import kotlinx.serialization.json.*

object NearbyScanHost {
    fun facts(): JsonObject = buildJsonObject {
        put("paused", bleTransport().createScanner().isScanPaused())
    }
}
