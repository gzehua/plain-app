package com.ismartcoding.plain.graphql.models

import kotlinx.serialization.Serializable

@Serializable
data class FileHostCompletedOp(
    var src: String,
    var dst: String,
)
