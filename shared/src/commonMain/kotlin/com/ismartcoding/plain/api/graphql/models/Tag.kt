package com.ismartcoding.plain.api.graphql.models

import kotlinx.serialization.Serializable

@Serializable
data class Tag(
    var id: String,
    var name: String,
    var count: Int,
    var type: Int = 0,
)
