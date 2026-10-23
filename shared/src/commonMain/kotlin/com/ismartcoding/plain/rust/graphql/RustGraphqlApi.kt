package com.ismartcoding.plain.rust.graphql

import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.rust.native.RustNativeApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/** Executes App GraphQL requests through the native bridge. */
object RustGraphqlApi {
    suspend inline fun <reified T> readSavedValue(key: String, default: T): T {
        val value = graphql("query(\$key: String!) { savedValue(key: \$key) }", mapOf("key" to JsonHelper.jsonEncodeToElement(key))).getValue("savedValue")
        return if (value == JsonNull) default else JsonHelper.jsonDecodeFromElement(value)
    }

    suspend inline fun <reified T> writeSavedValue(key: String, value: T) {
        graphql("mutation(\$key: String!, \$value: JSON!) { setSavedValue(key: \$key, value: \$value) }", mapOf("key" to JsonHelper.jsonEncodeToElement(key), "value" to JsonHelper.jsonEncodeToElement(value)))
    }

    suspend fun query(selection: String): JsonObject = graphql("query { $selection }")

    suspend fun mutate(selection: String): JsonObject = graphql("mutation { $selection }")

    suspend fun graphql(document: String, variables: Map<String, JsonElement> = emptyMap()): JsonObject =
        withContext(Dispatchers.IO) {
            val response = JsonHelper.jsonDecode<JsonObject>(
                RustNativeApi.graphql(JsonHelper.jsonEncode(GraphqlRequest(document, variables))),
            )
            response["errors"]?.let { errors ->
                error(errors.jsonArray.joinToString { it.jsonObject.getValue("message").jsonPrimitive.content })
            }
            response.getValue("data").jsonObject
        }
}
