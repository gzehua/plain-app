package com.ismartcoding.plain.rust.graphql

import com.ismartcoding.plain.graphql.Mutation
import com.ismartcoding.plain.graphql.Query
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.rust.native.RustNativeBindings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object RustGraphqlApi {
    inline fun <reified T> subscribe(request: GraphqlRequest): Flow<GraphqlResult<T>> =
        RustGraphqlSubscriptions.subscribe(request).map { response ->
            GraphqlResult(response.data?.let { JsonHelper.jsonDecodeFromElement<T>(it) }, response.errors)
        }

    suspend inline fun <reified T> readSavedValue(key: String, default: T): T {
        val value = graphql(Query.savedValue(key)).getValue("savedValue")
        return if (value == JsonNull) default else JsonHelper.jsonDecodeFromElement(value)
    }

    suspend inline fun <reified T> writeSavedValue(key: String, value: T) {
        graphql(Mutation.setSavedValue(key, JsonHelper.jsonEncodeToElement(value)))
    }

    suspend fun query(selection: String): JsonObject = graphql(GraphqlRequest($$"query { $${selection} }"))

    suspend fun mutate(selection: String): JsonObject = graphql(GraphqlRequest($$"mutation { $${selection} }"))

    suspend fun graphql(document: String, variables: Map<String, JsonElement> = emptyMap()): JsonObject =
        graphql(GraphqlRequest(document, variables))

    suspend fun graphql(request: GraphqlRequest): JsonObject =
        withContext(Dispatchers.IO) {
            val response = JsonHelper.jsonDecode<JsonObject>(
                RustNativeBindings.graphql(JsonHelper.jsonEncode(request)),
            )
            response["errors"]?.let { errors ->
                error(errors.jsonArray.joinToString { it.jsonObject.getValue("message").jsonPrimitive.content })
            }
            response.getValue("data").jsonObject
        }
}
