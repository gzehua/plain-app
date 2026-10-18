package com.ismartcoding.plain.features.nearby

import com.ismartcoding.plain.app.events.EventType
import com.ismartcoding.plain.app.events.HostEvent
import com.ismartcoding.plain.rust.PlainRust
import kotlinx.serialization.json.*

object BleScanReports {
    suspend fun scanState(id: String, active: Boolean) {
        PlainRust.publish(HostEvent(EventType.BLE_SCAN_STATE_REPORTED, buildJsonObject { put("scanId", id); put("active", active) }.toString()))
    }
    suspend fun scanSeen(id: String, shortId: String) {
        PlainRust.publish(HostEvent(EventType.BLE_ADVERTISEMENT_REPORTED, buildJsonObject { put("scanId", id); put("shortId", shortId) }.toString()))
    }
}
