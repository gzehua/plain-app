package com.ismartcoding.plain.api.graphql.models

import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
data class AudioPlaylist(
    var id: String,
    var name: String,
    var itemCount: Int,
    var createdAt: Instant,
    var updatedAt: Instant,
)
