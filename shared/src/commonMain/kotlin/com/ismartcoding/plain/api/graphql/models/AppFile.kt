package com.ismartcoding.plain.api.graphql.models

import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
data class AppFile(
    var id: String,
    var size: Long,
    var mimeType: String,
    var realPath: String,
    var fileName: String,
    var createdAt: Instant,
    var updatedAt: Instant,
)
