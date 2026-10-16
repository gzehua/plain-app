package com.ismartcoding.plain.api.graphql.models

import kotlinx.serialization.Serializable

@Serializable
data class ImageHostIndexStatus(
    var version: Long,
    var isRunning: Boolean,
    var totalImages: Long,
    var indexedImages: Long,
    var skippedImages: Long,
    var errorMessage: String,
)
