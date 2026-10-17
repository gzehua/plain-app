package com.ismartcoding.plain.api.graphql.models

import kotlinx.serialization.Serializable

@Serializable
data class FileTaskCompletedOp(
    var src: String,
    var dst: String,
)
