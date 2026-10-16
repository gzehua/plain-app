package com.ismartcoding.plain.api.graphql.models

import kotlinx.serialization.Serializable

@Serializable
data class ActionResult(
    var affectedCount: Int,
)
