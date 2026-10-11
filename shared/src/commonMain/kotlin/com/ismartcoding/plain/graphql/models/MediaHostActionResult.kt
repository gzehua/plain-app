package com.ismartcoding.plain.graphql.models

import kotlinx.serialization.Serializable

@Serializable
data class MediaHostActionResult(
    var affectedCount: Int,
    var failedIds: List<String>,
)
