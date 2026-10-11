package com.ismartcoding.plain.graphql.models

import kotlinx.serialization.Serializable

@Serializable
data class TagHostItemInput(
    var key: String,
    var sizeBytes: Long,
    var title: String,
)
