package com.ismartcoding.plain.api.graphql.models

import kotlinx.serialization.Serializable

@Serializable
data class MediaHostActionResult(
    var affectedCount: Int,
    var failedIds: List<String>,
)
