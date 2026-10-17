package com.ismartcoding.plain.api.graphql.models

import kotlin.time.Instant

interface MediaItem : com.ismartcoding.plain.db.IData {
    override var id: String
    val title: String
    val path: String
    val size: Long
    val bucketId: String
    val createdAt: Instant
    val updatedAt: Instant
}
