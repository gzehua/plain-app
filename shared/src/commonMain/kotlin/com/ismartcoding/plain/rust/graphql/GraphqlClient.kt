package com.ismartcoding.plain.rust.graphql

import com.ismartcoding.plain.lib.JsonHelper

open class GraphqlClient internal constructor(
    @PublishedApi internal val send: suspend (GraphqlRequest) -> GraphqlResponse,
) {
    suspend inline fun <reified T> execute(request: GraphqlRequest): T {
        val response = send(request)
        if (response.errors.isNotEmpty()) {
            error(response.errors.joinToString { it.message })
        }
        val data = checkNotNull(response.data) { "GraphQL response has no data" }
        if (T::class == Unit::class) return Unit as T
        val value = request.field?.let { data.getValue(it) } ?: data
        return JsonHelper.jsonDecodeFromElement(value)
    }
}
