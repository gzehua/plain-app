package com.ismartcoding.plain.graphql.models

import kotlinx.serialization.Serializable

@Serializable
data class FavoriteFolder(
    var rootPath: String,
    var fullPath: String,
    var alias: String? = null,
)
