package com.ismartcoding.plain.graphql.models

import kotlinx.serialization.Serializable

@Serializable
data class DbTableInfo(
    var idKey: String,
)
