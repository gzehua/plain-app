package com.ismartcoding.plain.api.graphql.models

import kotlinx.serialization.Serializable

@Serializable
data class FileHostCompletedOp(
    var src: String,
    var dst: String,
)
