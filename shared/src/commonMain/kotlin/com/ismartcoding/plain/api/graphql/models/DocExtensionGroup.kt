package com.ismartcoding.plain.api.graphql.models

import kotlinx.serialization.Serializable

@Serializable
data class DocExtensionGroup(
    val ext: String,
    val count: Int,
)
