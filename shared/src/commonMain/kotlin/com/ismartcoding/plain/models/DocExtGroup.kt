package com.ismartcoding.plain.models

import kotlinx.serialization.Serializable

@Serializable
data class DocExtGroup(
    var ext: String,
    var count: Int,
)
