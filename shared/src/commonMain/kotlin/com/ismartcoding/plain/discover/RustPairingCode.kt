package com.ismartcoding.plain.discover

import com.ismartcoding.plain.graphql.models.NearbyDevice
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.rust.PlainRust
import com.ismartcoding.plain.rust.content.gql
import kotlinx.serialization.json.JsonNull

object RustPairingCode {
    suspend fun parse(code: String): NearbyDevice? {
        val value = PlainRust.mainQuery("parsePairingCode(code: ${gql(code)}) { id name ips port deviceType version platform lastSeen discoveryMethods bestIp status }").getValue("parsePairingCode")
        return if (value is JsonNull) null else JsonHelper.jsonDecodeFromElement(value)
    }
}
