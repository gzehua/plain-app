package com.ismartcoding.plain.api.graphql.models

import kotlinx.serialization.Serializable

@Serializable
data class DbTableInfo(
    var idKey: String,
)
