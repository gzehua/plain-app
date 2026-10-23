package com.ismartcoding.plain.rust.graphql

import com.ismartcoding.plain.lib.JsonHelper
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.JsonElement

@Serializable
data class GraphqlRequest(
    val query: String,
    val variables: Map<String, JsonElement> = emptyMap(),
    val operationName: String? = null,
    @Transient val field: String = "",
)

internal inline fun <reified T> variable(name: String, value: T): Pair<String, JsonElement> =
    name to JsonHelper.jsonEncodeToElement(value)
