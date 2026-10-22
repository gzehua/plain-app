package com.ismartcoding.plain.rust.graphql

import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.rust.native.RustDispatchers
import com.ismartcoding.plain.rust.native.RustNativeApi
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

/** Executes App GraphQL requests through the native bridge. */
object RustGraphqlApi {
    suspend fun query(selection: String): JsonObject = graphql("query { $selection }")

    suspend fun mutate(selection: String): JsonObject = graphql("mutation { $selection }")

    suspend fun graphql(document: String, variables: Map<String, JsonElement> = emptyMap()): JsonObject =
        withContext(RustDispatchers.requests) {
            val response = RustNativeApi.graphql(JsonHelper.jsonEncode(GraphqlRequest(document, variables)))
            unwrap(JsonHelper.jsonDecode(response))
        }

    private fun unwrap(response: JsonObject): JsonObject {
        val envelope = JsonHelper.jsonDecodeFromElement<GraphqlEnvelope>(response)
        check(envelope.errors.isNullOrEmpty()) { envelope.errors?.joinToString { it.message ?: "Rust API error" } ?: "Rust API error" }
        return requireNotNull(envelope.data) { "Rust API response missing data" }
    }
}

@Serializable
private data class GraphqlEnvelope(val data: JsonObject? = null, val errors: List<GraphqlError>? = null)

@Serializable
private data class GraphqlError(val message: String? = null)
