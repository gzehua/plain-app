package com.ismartcoding.plain.api.graphql.models

import kotlinx.serialization.Serializable

@Serializable
data class DocExtGroup(
    var ext: String,
    var count: Int,
)
