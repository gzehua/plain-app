package com.ismartcoding.plain.models

import kotlinx.serialization.Serializable

@Serializable
data class SelectedMediaPath(
    val id: String,
    val path: String,
)
