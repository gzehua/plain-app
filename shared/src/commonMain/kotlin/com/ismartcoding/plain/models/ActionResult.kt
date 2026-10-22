package com.ismartcoding.plain.models

import kotlinx.serialization.Serializable

@Serializable
data class ActionResult(
    var affectedCount: Int,
)
