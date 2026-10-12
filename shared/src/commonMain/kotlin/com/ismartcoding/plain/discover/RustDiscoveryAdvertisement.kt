package com.ismartcoding.plain.discover

import com.ismartcoding.plain.helpers.Base64Lenient
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.rust.bridge.RustDiscoveryBridge
import kotlinx.serialization.json.*

object RustDiscoveryAdvertisement {
    private fun factsJson(): String = JsonHelper.jsonEncode(DiscoveryAdvertisementHost.facts())
    private fun call(request: String): String =
        RustDiscoveryBridge.action(factsJson(), request)

    suspend fun replyJson(): String = call("""{"action":"reply"}""")
    suspend fun reply(): DDiscoverReply = JsonHelper.jsonDecode(replyJson())
    suspend fun ble(): ByteArray = Base64Lenient.decode(
        call("""{"action":"ble"}""").trim('"'),
    )
    suspend fun mdns(): MdnsServiceInfo {
        val row = JsonHelper.jsonDecode<JsonObject>(call("""{"action":"mdns"}"""))
        fun string(key: String) = row.getValue(key).jsonPrimitive.content
        fun strings(key: String) = row.getValue(key).jsonArray.map { it.jsonPrimitive.content }
        return MdnsServiceInfo(
            string("instanceName"),
            string("serviceType"),
            string("targetHostname"),
            row.getValue("port").jsonPrimitive.int,
            strings("txtRecords"),
            strings("ips"),
        )
    }
}