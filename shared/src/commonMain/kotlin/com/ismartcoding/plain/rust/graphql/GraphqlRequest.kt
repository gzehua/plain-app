package com.ismartcoding.plain.rust.graphql

import com.ismartcoding.plain.lib.JsonHelper
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

class GraphqlRequest<out T> @PublishedApi internal constructor(
    internal val operation: GraphqlOperation,
    internal val field: String?,
    internal val decode: (JsonObject) -> T,
) {
    @PublishedApi
    internal fun <R> map(transform: (T) -> R): GraphqlRequest<R> =
        GraphqlRequest(operation, field) { transform(decode(it)) }
}

inline fun <reified T> GraphqlRequest(
    query: String,
    variables: Map<String, JsonElement> = emptyMap(),
    operationName: String? = null,
    field: String? = null,
): GraphqlRequest<T> = GraphqlRequest(GraphqlOperation(query, variables, operationName), field) { data ->
    if (T::class == Unit::class) Unit as T
    else JsonHelper.jsonDecodeFromElement(field?.let { data.getValue(it) } ?: data)
}

@PublishedApi
internal inline fun <reified T> variable(name: String, value: T): Pair<String, JsonElement> =
    name to JsonHelper.jsonEncodeToElement(value)
