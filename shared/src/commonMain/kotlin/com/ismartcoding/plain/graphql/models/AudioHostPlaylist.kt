package com.ismartcoding.plain.graphql.models

import com.ismartcoding.plain.lib.TimeHelper
import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
data class AudioHostPlaylist(
    var id: String,
    var name: String,
    var itemCount: Int = 0,
    @kotlinx.serialization.Required var createdAt: Instant = TimeHelper.now(),
    @kotlinx.serialization.Required var updatedAt: Instant = TimeHelper.now(),
)
