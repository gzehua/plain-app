package com.ismartcoding.plain.rust

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
internal data class RustStartupConfig(
    val debug: Boolean,
    val systemProperties: Map<String, JsonElement>,
)
