package com.ismartcoding.plain.rust.http

import kotlinx.serialization.Serializable

@Serializable
internal data class RustWebAssetsSource(
    val directory: String = "",
    val archives: List<String> = emptyList(),
    val prefix: String = "",
)

internal expect fun webAssetsSource(): RustWebAssetsSource
