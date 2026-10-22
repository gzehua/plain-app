package com.ismartcoding.plain.rust

import kotlinx.serialization.Serializable

@Serializable
internal data class RustStartupConfig(
    val debug: Boolean,
    val systemProperties: Map<String, kotlinx.serialization.json.JsonElement>,
)
