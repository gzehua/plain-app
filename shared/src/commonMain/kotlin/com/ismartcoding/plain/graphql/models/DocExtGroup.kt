package com.ismartcoding.plain.graphql.models

import kotlinx.serialization.Serializable

@Serializable
data class DocExtGroup(
    var ext: String,
    var count: Int,
)
