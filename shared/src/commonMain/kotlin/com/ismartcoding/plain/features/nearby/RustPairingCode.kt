package com.ismartcoding.plain.features.nearby

import com.ismartcoding.plain.rust.graphql.RustGraphqlApi

import com.ismartcoding.plain.models.NearbyDevice
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.rust.graphql.gql
import kotlinx.serialization.json.JsonNull

object RustPairingCode {
    suspend fun parse(code: String): NearbyDevice? {
        val value = RustGraphqlApi.query("parsePairingCode(code: ${gql(code)}) { id name ips port deviceType version platform lastSeen discoveryMethods bestIp status }").getValue("parsePairingCode")
        return if (value is JsonNull) null else JsonHelper.jsonDecodeFromElement(value)
    }
}
