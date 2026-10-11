package com.ismartcoding.plain.graphql.models

import kotlinx.serialization.Serializable

@Serializable
data class TagRelation(
    var tagId: String,
    var key: String,
)
