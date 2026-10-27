package com.ismartcoding.plain.features.nearby

import com.ismartcoding.plain.graphql.Query
import com.ismartcoding.plain.models.NearbyDevice
import com.ismartcoding.plain.rust.graphql.RustGraphqlApi

object RustPairingCode {
    suspend fun parse(code: String): NearbyDevice? =
        RustGraphqlApi.execute(Query.parsePairingCode(code))
}
