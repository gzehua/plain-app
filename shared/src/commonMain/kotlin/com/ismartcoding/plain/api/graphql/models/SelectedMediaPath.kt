package com.ismartcoding.plain.api.graphql.models

import kotlinx.serialization.Serializable

@Serializable
data class SelectedMediaPath(
    val id: String,
    val path: String,
)
