package com.ismartcoding.plain.graphql.models

import com.ismartcoding.plain.ai.ImageSearchStatusType
import kotlinx.serialization.Serializable

@Serializable
data class ImageSearchStatus(
    var status: ImageSearchStatusType,
    var downloadProgress: Int,
    var errorMessage: String,
    var modelSize: Long,
    var modelDir: String,
    var isIndexing: Boolean,
    var totalImages: Int,
    var indexedImages: Int,
)
