package com.ismartcoding.plain.rust.request

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class GraphqlRequest(
    val query: String,
    val variables: Map<String, JsonElement> = emptyMap(),
    val operationName: String? = null
)
