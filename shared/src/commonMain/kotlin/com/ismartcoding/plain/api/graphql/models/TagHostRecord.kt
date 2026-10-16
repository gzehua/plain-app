package com.ismartcoding.plain.api.graphql.models

import com.ismartcoding.plain.lib.TimeHelper
import com.ismartcoding.plain.lib.generateId
import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
data class TagHostRecord(
    @kotlinx.serialization.Required override var id: String = generateId(),
    @kotlinx.serialization.Required var name: String = "",
    @kotlinx.serialization.Required var type: Int = 0,
    @kotlinx.serialization.Required var count: Int = 0,
    @kotlinx.serialization.Required var createdAt: Instant = TimeHelper.now(),
    @kotlinx.serialization.Required var updatedAt: Instant = TimeHelper.now(),
) : com.ismartcoding.plain.db.IData
