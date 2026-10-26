package com.ismartcoding.plain.features.nearby

import com.ismartcoding.plain.rust.sendEventToRust

import com.ismartcoding.plain.lib.JsonHelper
import kotlinx.serialization.Serializable

import com.ismartcoding.plain.events.EventType
import com.ismartcoding.plain.events.HostEvent

object BleScanReports {
    suspend fun scanState(id: String, active: Boolean) {
        sendEventToRust(HostEvent(EventType.BLE_SCAN_STATE_REPORTED, JsonHelper.jsonEncode(ScanState(id, active))))
    }
    suspend fun scanSeen(id: String, shortId: String) {
        sendEventToRust(HostEvent(EventType.BLE_ADVERTISEMENT_REPORTED, JsonHelper.jsonEncode(ScanSeen(id, shortId))))
    }
}

@Serializable
private data class ScanState(val scanId: String, val active: Boolean)

@Serializable
private data class ScanSeen(val scanId: String, val shortId: String)
