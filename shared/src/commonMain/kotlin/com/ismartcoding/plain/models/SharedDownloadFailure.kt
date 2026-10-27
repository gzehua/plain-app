package com.ismartcoding.plain.models

import kotlinx.serialization.Serializable

@Serializable
data class SharedDownloadFailure(
    val path: String,
    val error: String,
)
