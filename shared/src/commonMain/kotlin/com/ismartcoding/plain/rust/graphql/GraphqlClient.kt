package com.ismartcoding.plain.rust.graphql

open class GraphqlClient internal constructor(
    private val send: suspend (GraphqlOperation) -> GraphqlResponse,
) {
    suspend fun <T> execute(request: GraphqlRequest<T>): T {
        val response = send(request.operation)
        if (response.errors.isNotEmpty()) {
            error(response.errors.joinToString { it.message })
        }
        val data = checkNotNull(response.data) { "GraphQL response has no data" }
        return request.decode(data)
    }
}
