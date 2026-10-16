package com.ismartcoding.plain.api.graphql.models

import kotlinx.serialization.Serializable

@Serializable
data class ContactSource(
    var name: String,
    var type: String,
)
