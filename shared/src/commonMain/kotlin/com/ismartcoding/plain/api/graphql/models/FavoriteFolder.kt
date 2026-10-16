package com.ismartcoding.plain.api.graphql.models

import kotlinx.serialization.Serializable

@Serializable
data class FavoriteFolder(
    var rootPath: String,
    var fullPath: String,
    var alias: String? = null,
)
