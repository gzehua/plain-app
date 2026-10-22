package com.ismartcoding.plain.models

import kotlinx.serialization.Serializable

@Serializable
data class BookmarkInput(
    var url: String,
    var title: String,
    var groupId: String,
    var pinned: Boolean,
    var sortOrder: Int,
)
