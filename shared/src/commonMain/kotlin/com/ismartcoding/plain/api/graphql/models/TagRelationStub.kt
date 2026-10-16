package com.ismartcoding.plain.api.graphql.models

import kotlinx.serialization.Serializable

@Serializable
data class TagRelationStub(
    var key: String,
    var title: String,
    var size: Long,
)
