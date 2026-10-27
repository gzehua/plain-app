package com.ismartcoding.plain.rust.graphql

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject

@Serializable
data class GraphqlError(val message: String, val path: JsonArray? = null, val locations: List<GraphqlErrorLocation> = emptyList(), val extensions: JsonObject? = null)
@Serializable
data class GraphqlErrorLocation(val line: Int, val column: Int)
@Serializable
data class GraphqlResponse(val data: JsonObject? = null, val errors: List<GraphqlError> = emptyList(), val extensions: JsonObject? = null)

class GraphqlSubscriptionException(val errors: List<GraphqlError>) : IllegalStateException(errors.joinToString { it.message }) {
    val requiresResynchronization: Boolean get() = errors.any { it.message == "resync_required" }
}
