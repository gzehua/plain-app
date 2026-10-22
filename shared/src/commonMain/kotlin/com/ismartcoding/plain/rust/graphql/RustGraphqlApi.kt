package com.ismartcoding.plain.rust.graphql

import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.network.PlainHttpClientSpec
import com.ismartcoding.plain.network.createPlainHttpClient
import com.ismartcoding.plain.network.postText
import com.ismartcoding.plain.rust.native.RustNativeApi
import com.ismartcoding.plain.rust.native.RustDispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

/** Uses the native bridge locally; an explicit session selects authenticated HTTP. */
object RustGraphqlApi {
    private val client by lazy { createPlainHttpClient(PlainHttpClientSpec.Local()) }
    private val transferClient by lazy { createPlainHttpClient(PlainHttpClientSpec.Local(15 * 60)) }

    suspend fun query(selection: String, session: ApiSession? = null): JsonObject =
        data("query { $selection }", session)

    suspend fun mutate(selection: String, session: ApiSession? = null, longRunning: Boolean = false): JsonObject =
        data("mutation { $selection }", session, longRunning = longRunning)

    suspend fun graphql(document: String, variables: Map<String, JsonElement> = emptyMap(), session: ApiSession? = null, longRunning: Boolean = false): JsonObject =
        data(document, session, variables = variables, longRunning = longRunning)

    private suspend fun data(
        document: String,
        session: ApiSession?,
        variables: Map<String, JsonElement> = emptyMap(),
        operationName: String? = null,
        longRunning: Boolean = false,
    ): JsonObject = unwrap(if (session == null) {
        native(document, variables, operationName)
    } else {
        remote(document, variables, operationName, session, longRunning)
    })

    private suspend fun native(document: String, variables: Map<String, JsonElement>, operationName: String?): JsonObject =
        withContext(RustDispatchers.requests) {
            JsonHelper.jsonDecode(RustNativeApi.graphql(JsonHelper.jsonEncode(GraphqlRequest(document, variables, operationName))))
        }

    private suspend fun remote(
        document: String,
        variables: Map<String, JsonElement>,
        operationName: String?,
        session: ApiSession,
        longRunning: Boolean,
    ): JsonObject {
        val body = JsonHelper.jsonEncode(GraphqlRequest(document, variables, operationName))
        val response = (if (longRunning) transferClient else client)
            .postText("${session.baseUrl}/graphql", body, "application/json", session.headers())
        response.use {
            check(it.isOk()) { "Rust API returned HTTP ${it.status}" }
            return JsonHelper.jsonDecode(it.bodyAsText())
        }
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
