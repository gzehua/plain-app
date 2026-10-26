package com.ismartcoding.plain.features.nearby

import com.ismartcoding.plain.graphql.Query
import com.ismartcoding.plain.rust.graphql.RustGraphqlApi
import com.ismartcoding.plain.models.NearbyDevice
import com.ismartcoding.plain.lib.JsonHelper
import kotlinx.serialization.json.JsonNull

object RustPairingCode {
    suspend fun parse(code: String): NearbyDevice? {
        val value = RustGraphqlApi.graphql(Query.parsePairingCode(code)).getValue("parsePairingCode")
        return if (value is JsonNull) null else JsonHelper.jsonDecodeFromElement(value)
    }
}
