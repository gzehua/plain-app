package com.ismartcoding.plain.api.graphql.models

import com.ismartcoding.plain.lib.TimeHelper
import com.ismartcoding.plain.lib.generateId
import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Bookmark(
    @kotlinx.serialization.Required override var id: String = generateId(),
    @kotlinx.serialization.Required var url: String = "",
    @kotlinx.serialization.Required var title: String = "",
    @kotlinx.serialization.Required var faviconPath: String = "",
    @kotlinx.serialization.Required var groupId: String = "",
    @kotlinx.serialization.Required var pinned: Boolean = false,
    @kotlinx.serialization.Required var clickCount: Int = 0,
    @kotlinx.serialization.Required var lastClickedAt: Instant? = null,
    @kotlinx.serialization.Required var sortOrder: Int = 0,
    @kotlinx.serialization.Required var createdAt: Instant = TimeHelper.now(),
    @kotlinx.serialization.Required var updatedAt: Instant = TimeHelper.now(),
) : com.ismartcoding.plain.db.IData
