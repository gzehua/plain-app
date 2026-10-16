package com.ismartcoding.plain.api.graphql.models

import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
data class File(
    var mediaId: String? = null,
    var name: String,
    var path: String,
    var createdAt: Instant?,
    var updatedAt: Instant,
    var size: Long,
    var isDir: Boolean,
    var childCount: Int,
    var permission: String = "",
) : com.ismartcoding.plain.db.IData {
    override var id: String
        get() = path
        set(value) { path = value }
}
