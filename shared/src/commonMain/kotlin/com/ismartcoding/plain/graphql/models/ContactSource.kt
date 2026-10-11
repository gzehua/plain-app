package com.ismartcoding.plain.graphql.models

import kotlinx.serialization.Serializable

@Serializable
data class ContactSource(
    var name: String,
    var type: String,
)
