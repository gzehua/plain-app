package com.ismartcoding.plain.platform

import kotlinx.serialization.Serializable

@Serializable
internal data class RustHttpServerResult(
    val httpPort: Int = 0,
    val httpsPort: Int = 0,
    val generation: Long = 0,
    val cacheDir: String = "",
    val imageCacheDir: String = "",
    val videoCacheDir: String = "",
    val errorCode: String = "",
    val error: String = "",
)
