package com.ismartcoding.plain.models

import com.ismartcoding.plain.IData
import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
data class BookmarkGroup(
    override var id: String,
    var name: String,
    var collapsed: Boolean,
    var sortOrder: Int,
    var itemCount: Int = 0,
    var createdAt: Instant,
    var updatedAt: Instant,
) : IData
