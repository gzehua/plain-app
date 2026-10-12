package com.ismartcoding.plain.discover

import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.rust.bridge.RustDiscoveryBridge
import kotlinx.serialization.json.*

object RustBleServiceData {
    private val EMPTY_FACTS = "{}"

    suspend fun decode(data: ByteArray?): BleAdvertisement? {
        val payload = data?.joinToString(",") { (it.toInt() and 255).toString() }?.let { "[$it]" }
        val request = buildJsonObject {
            put("action", "bleDecode")
            payload?.let { put("payload", Json.parseToJsonElement(it)) } ?: put("payload", JsonNull)
        }
        val raw = RustDiscoveryBridge.action(EMPTY_FACTS, request.toString())
        if (raw.isBlank() || raw == "null") return null
        val parsed = JsonHelper.jsonDecode<JsonElement>(raw)
        return if (parsed is JsonNull) null else JsonHelper.jsonDecode<BleAdvertisement>(raw)
    }

    suspend fun shortIdOf(clientId: String): String {
        val request = buildJsonObject { put("action", "bleShortId"); put("id", clientId) }
        val raw = RustDiscoveryBridge.action(EMPTY_FACTS, request.toString())
        return JsonHelper.jsonDecode<JsonElement>(raw).jsonPrimitive.content
    }
}