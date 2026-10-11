package com.ismartcoding.plain.graphql.models

import kotlinx.serialization.Serializable

@Serializable
data class FileHostTaskOpInput(
    var src: String,
    var dst: String,
    var overwrite: Boolean,
)
