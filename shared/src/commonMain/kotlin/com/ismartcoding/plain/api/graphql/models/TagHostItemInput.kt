package com.ismartcoding.plain.api.graphql.models

import kotlinx.serialization.Serializable

@Serializable
data class TagHostItemInput(
    var key: String,
    var sizeBytes: Long,
    var title: String,
)
