package com.ismartcoding.plain.features.telephony

import com.ismartcoding.plain.rust.graphql.RustGraphqlApi

import com.ismartcoding.plain.rust.graphql.gql
import com.ismartcoding.plain.lib.JsonHelper
import kotlinx.serialization.json.*

object CallsClient {
    suspend fun search(query: String, limit: Int, offset: Int): List<com.ismartcoding.plain.models.Call> =
        JsonHelper.jsonDecodeFromElement(RustGraphqlApi.query("calls(query: ${gql(query)}, limit: $limit, offset: $offset) { id number name photoId photoUri startedAt durationSec type typeCode accountId }").getValue("calls"))
    suspend fun count(query: String): Int = RustGraphqlApi.query("callCount(query: ${gql(query)})").getValue("callCount").jsonPrimitive.int
}
