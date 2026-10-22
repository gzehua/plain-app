package com.ismartcoding.plain.rust.graphql

import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.rust.native.RustDispatchers
import com.ismartcoding.plain.rust.native.RustNativeApi
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/** Executes App GraphQL requests through the native bridge. */
object RustGraphqlApi {
    suspend fun query(selection: String): JsonObject = graphql("query { $selection }")

    suspend fun mutate(selection: String): JsonObject = graphql("mutation { $selection }")

    suspend fun graphql(document: String, variables: Map<String, JsonElement> = emptyMap()): JsonObject =
        withContext(RustDispatchers.requests) {
            val response = JsonHelper.jsonDecode<JsonObject>(
                RustNativeApi.graphql(JsonHelper.jsonEncode(GraphqlRequest(document, variables))),
            )
            response["errors"]?.let { errors ->
                error(errors.jsonArray.joinToString { it.jsonObject.getValue("message").jsonPrimitive.content })
            }
            response.getValue("data").jsonObject
        }
}
