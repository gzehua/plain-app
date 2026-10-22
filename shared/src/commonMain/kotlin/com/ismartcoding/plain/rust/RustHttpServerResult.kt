package com.ismartcoding.plain.rust


import kotlinx.serialization.Serializable

@Serializable
internal data class RustHttpServerResult(
    val logFile: String = "",
    val cacheDir: String = "",
    val imageCacheDir: String = "",
    val videoCacheDir: String = "",
    val sharedPreviewDir: String = "",
    val errorCode: String = "",
    val error: String = "",
)
