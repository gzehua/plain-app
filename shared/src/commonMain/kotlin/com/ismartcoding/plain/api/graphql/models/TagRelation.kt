package com.ismartcoding.plain.api.graphql.models

import kotlinx.serialization.Serializable

@Serializable
data class TagRelation(
    var tagId: String,
    var key: String,
)
