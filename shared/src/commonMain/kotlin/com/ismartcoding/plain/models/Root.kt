package com.ismartcoding.plain.models

import kotlinx.serialization.Serializable

@Serializable
data class Root(
    var virtualPath: String,
    var realPath: String,
    var isDir: Boolean,
)
