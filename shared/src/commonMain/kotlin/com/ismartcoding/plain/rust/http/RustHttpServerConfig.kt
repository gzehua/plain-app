package com.ismartcoding.plain.rust.http

import kotlinx.serialization.Serializable

@Serializable
internal data class RustHttpServerConfig(
    val debug: Boolean,
    val systemProperties: Map<String, kotlinx.serialization.json.JsonElement>,
)
