package com.ismartcoding.plain.models

import kotlinx.serialization.Serializable

@Serializable
data class TagRelation(
    var tagId: String,
    var key: String,
)
