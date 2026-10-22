package com.ismartcoding.plain.features.nearby

import com.ismartcoding.plain.rust.RustEventHandler

import com.ismartcoding.plain.lib.JsonHelper
import kotlinx.serialization.Serializable

import com.ismartcoding.plain.app.events.EventType
import com.ismartcoding.plain.app.events.HostEvent
import kotlinx.serialization.json.*

object BleScanReports {
    suspend fun scanState(id: String, active: Boolean) {
        RustEventHandler.sendEvent(HostEvent(EventType.BLE_SCAN_STATE_REPORTED, JsonHelper.jsonEncode(ScanState(id, active))))
    }
    suspend fun scanSeen(id: String, shortId: String) {
        RustEventHandler.sendEvent(HostEvent(EventType.BLE_ADVERTISEMENT_REPORTED, JsonHelper.jsonEncode(ScanSeen(id, shortId))))
    }
}

@Serializable
private data class ScanState(val scanId: String, val active: Boolean)

@Serializable
private data class ScanSeen(val scanId: String, val shortId: String)
