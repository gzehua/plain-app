package com.ismartcoding.plain.discover

import com.ismartcoding.plain.TempData
import com.ismartcoding.plain.lib.crypto.sha256
import com.ismartcoding.plain.platform.getAppVersion
import com.ismartcoding.plain.platform.getDeviceIP4s
import com.ismartcoding.plain.platform.getDeviceName
import com.ismartcoding.plain.platform.getDeviceType
import com.ismartcoding.plain.platform.getPlatformName
import com.ismartcoding.plain.platform.isWifiAwareSupported
import com.ismartcoding.plain.preferences.UserPrefs
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

private const val PLAINAPP_SERVICE_TYPE = "_plainapp._tcp.local"
private const val DEFAULT_MDNS_HOSTNAME = "plainapp.local"

/**
 * Pure-Kotlin mirror of `plain_rs::discovery::advertisement`. The wire format
 * (reply JSON, BLE 9-byte payload, mDNS service info) is local to this file so
 * the BLE/mDNS scanners and the peer wire protocol must agree. The previous
 * FFI pulled these helpers out of plain-rs but they are pure functions with no
 * server-runtime state, so reproducing them here keeps the build simple at
 * the cost of one extra copy of the format.
 */
object DiscoveryAdvertisement {
    private fun factsJson(): JsonObject = DiscoveryAdvertisementHost.facts()
    private fun stringField(obj: JsonObject, key: String): String =
        obj[key]?.jsonPrimitive?.content.orEmpty()

    fun replyJson(): String = buildJsonObject {
        put("id", TempData.clientId)
        val name = getDeviceName()
        put("name", if (name.isEmpty()) stringField(factsJson(), "name") else name)
        put("port", UserPrefs.httpsPort.value)
        put("deviceType", getDeviceType().name)
        put("version", getAppVersion())
        put("platform", getPlatformName())
        put("ips", JsonArray(getDeviceIP4s().map { kotlinx.serialization.json.JsonPrimitive(it) }))
        put("awareSupported", isWifiAwareSupported)
        put("awareRunning", TempData.awareRunning.value)
    }.toString()

    fun reply(): DDiscoverReply {
        val obj = Json.decodeFromString(JsonObject.serializer(), replyJson())
        return DDiscoverReply(
            id = stringField(obj, "id"),
            name = stringField(obj, "name"),
            port = obj["port"]?.jsonPrimitive?.int ?: 0,
            deviceType = runCatching { com.ismartcoding.plain.enums.DeviceType.valueOf(stringField(obj, "deviceType")) }
                .getOrDefault(com.ismartcoding.plain.enums.DeviceType.OTHER),
            version = stringField(obj, "version"),
            platform = stringField(obj, "platform"),
            ips = obj["ips"]?.let { (it as? JsonArray)?.map { e -> e.jsonPrimitive.content } } ?: emptyList(),
            awareSupported = obj["awareSupported"]?.jsonPrimitive?.boolean ?: false,
            awareRunning = obj["awareRunning"]?.jsonPrimitive?.boolean ?: false,
        )
    }

    /** 9 bytes: `flags(1) + sha256(id)[:8]`. flags = supported | (running << 1). */
    fun ble(): ByteArray {
        val id = TempData.clientId
        require(id.isNotEmpty()) { "Missing client identity" }
        val facts = factsJson()
        val supported = facts["awareSupported"]?.jsonPrimitive?.boolean ?: false
        val running = facts["awareRunning"]?.jsonPrimitive?.boolean ?: false
        val flags = (if (supported) 1 else 0) or (if (running) 2 else 0)
        val digest = sha256(id.toByteArray(Charsets.UTF_8))
        val out = ByteArray(9)
        out[0] = flags.toByte()
        System.arraycopy(digest, 0, out, 1, 8)
        return out
    }

    fun mdns(): MdnsServiceInfo {
        val r = reply()
        require(r.port > 0) { "Missing discovery HTTPS port" }
        val hostname = TempData.mdnsHostname.ifEmpty { DEFAULT_MDNS_HOSTNAME }
        return MdnsServiceInfo(
            instanceName = r.name,
            serviceType = PLAINAPP_SERVICE_TYPE,
            targetHostname = hostname,
            port = r.port,
            txtRecords = listOf(
                "id=${r.id}",
                "dv=${r.deviceType.name}",
                "ver=${r.version}",
                "pf=${r.platform}",
                "aw=${if (r.awareSupported) 1 else 0}",
                "ar=${if (r.awareRunning) 1 else 0}",
            ),
            ips = r.ips,
        )
    }
}