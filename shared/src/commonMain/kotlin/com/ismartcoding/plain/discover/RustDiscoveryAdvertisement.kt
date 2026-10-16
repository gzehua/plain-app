package com.ismartcoding.plain.discover

import com.ismartcoding.plain.helpers.Base64Lenient

import com.ismartcoding.plain.graphql.models.DiscoveryReply
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.rust.PlainRust
import kotlin.io.encoding.Base64
import kotlinx.serialization.json.jsonPrimitive

object RustDiscoveryAdvertisement {
    suspend fun replyJson(): String = PlainRust.mainQuery("discoveryReply { id name port deviceType version platform ips awareSupported awareRunning }").getValue("discoveryReply").toString()
    suspend fun reply(): DiscoveryReply = JsonHelper.jsonDecode(replyJson())
    suspend fun ble(): ByteArray = Base64.decode(PlainRust.mainQuery("discoveryBleAdvertisement").getValue("discoveryBleAdvertisement").jsonPrimitive.content)
}
