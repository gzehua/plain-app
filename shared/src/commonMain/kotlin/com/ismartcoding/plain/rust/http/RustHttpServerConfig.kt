package com.ismartcoding.plain.rust.http

import kotlinx.serialization.Serializable

@Serializable
internal data class RustHttpServerConfig(
    val debug: Boolean,
    val webAssets: RustWebAssetsSource,
    val systemProperties: Map<String, kotlinx.serialization.json.JsonElement>,
)
