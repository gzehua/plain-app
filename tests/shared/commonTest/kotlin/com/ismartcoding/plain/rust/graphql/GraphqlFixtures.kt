package com.ismartcoding.plain.rust.graphql

import kotlinx.serialization.json.JsonObject

internal fun graphqlClient(
    query: suspend (GraphqlRequest) -> JsonObject = { error("Unexpected query: ${it.query}") },
    mutate: suspend (GraphqlRequest) -> JsonObject = { error("Unexpected mutation: ${it.query}") },
): GraphqlClient = GraphqlClient { request ->
    val data = if (request.query.trimStart().startsWith("mutation")) mutate(request) else query(request)
    GraphqlResponse(data = data)
}
