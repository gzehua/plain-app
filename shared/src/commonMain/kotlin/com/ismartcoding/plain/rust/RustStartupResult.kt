package com.ismartcoding.plain.rust


import kotlinx.serialization.Serializable

@Serializable
internal data class RustStartupResult(
    val logFile: String = "",
    val sharedPreviewDir: String = "",
    val errorCode: String = "",
    val error: String = "",
)
