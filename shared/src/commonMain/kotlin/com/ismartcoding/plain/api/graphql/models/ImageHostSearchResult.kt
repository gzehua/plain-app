package com.ismartcoding.plain.api.graphql.models

import kotlinx.serialization.Serializable

@Serializable
data class ImageHostSearchResult(
    var imageId: String,
    var score: Double,
)
