package com.ismartcoding.plain.api.graphql.models

import kotlinx.serialization.Serializable

@Serializable
data class SharedDownloadFailure(
    val path: String,
    val error: String,
)
