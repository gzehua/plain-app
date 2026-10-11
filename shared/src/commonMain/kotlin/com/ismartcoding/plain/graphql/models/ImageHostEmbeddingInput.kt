package com.ismartcoding.plain.graphql.models

import kotlinx.serialization.Serializable

@Serializable
data class ImageHostEmbeddingInput(
    var id: String,
    var path: String,
    var embeddingBase64: String,
)
