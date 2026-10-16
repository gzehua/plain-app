package com.ismartcoding.plain.api.graphql.models

import com.ismartcoding.plain.lib.TimeHelper
import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
data class TagHostRelation(
    @kotlinx.serialization.Required var tagId: String = "",
    @kotlinx.serialization.Required var key: String = "",
    @kotlinx.serialization.Required var type: Int = 0,
    @kotlinx.serialization.Required var createdAt: Instant = TimeHelper.now(),
    @kotlinx.serialization.Required var sizeBytes: Long = 0,
    @kotlinx.serialization.Required var title: String = "",
)
