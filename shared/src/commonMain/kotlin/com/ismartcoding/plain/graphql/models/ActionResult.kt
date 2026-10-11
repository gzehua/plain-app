package com.ismartcoding.plain.graphql.models

import kotlinx.serialization.Serializable

@Serializable
data class ActionResult(
    var affectedCount: Int,
)
