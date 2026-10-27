package com.ismartcoding.plain.features.telephony

import com.ismartcoding.plain.graphql.Query
import com.ismartcoding.plain.models.Call
import com.ismartcoding.plain.rust.graphql.RustGraphqlApi

object CallsClient {
    suspend fun search(query: String, limit: Int, offset: Int): List<Call> =
        RustGraphqlApi.read(Query.calls(query, limit, offset))

    suspend fun count(query: String): Int = RustGraphqlApi.read(Query.callCount(query))
}
