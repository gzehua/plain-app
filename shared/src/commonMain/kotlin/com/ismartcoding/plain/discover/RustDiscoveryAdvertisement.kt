package com.ismartcoding.plain.discover

import com.ismartcoding.plain.helpers.Base64Lenient
import com.ismartcoding.plain.lib.JsonHelper

object RustDiscoveryAdvertisement {
    suspend fun replyJson(): String = DiscoveryAdvertisement.replyJson()
    suspend fun reply(): DDiscoverReply = DiscoveryAdvertisement.reply()
    suspend fun ble(): ByteArray = DiscoveryAdvertisement.ble()
    suspend fun mdns(): MdnsServiceInfo = DiscoveryAdvertisement.mdns()
}