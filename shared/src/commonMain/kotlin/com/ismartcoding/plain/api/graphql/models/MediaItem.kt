package com.ismartcoding.plain.api.graphql.models

import kotlin.time.Instant

interface MediaItem : com.ismartcoding.plain.db.IData, com.ismartcoding.plain.ui.state.IItemMetadata {
    override var id: String
    override val title: String
    val path: String
    override val size: Long
    val bucketId: String
    val createdAt: Instant
    val updatedAt: Instant
}
