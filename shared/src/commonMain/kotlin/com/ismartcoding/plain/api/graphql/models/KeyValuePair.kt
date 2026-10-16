package com.ismartcoding.plain.api.graphql.models

import kotlinx.serialization.Serializable

@Serializable
data class KeyValuePair(
    var key: String,
    var value: String,
)
