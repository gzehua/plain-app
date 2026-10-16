package com.ismartcoding.plain.platform

import kotlinx.serialization.Serializable

@Serializable
internal data class RustHttpServerConfig(
    val debug: Boolean,
    val webRoot: String,
    val systemProperties: Map<String, kotlinx.serialization.json.JsonElement>,
)
