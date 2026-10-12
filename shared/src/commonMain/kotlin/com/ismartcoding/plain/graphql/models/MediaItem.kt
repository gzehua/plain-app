package com.ismartcoding.plain.graphql.models

import kotlin.time.Instant

interface MediaItem {
    val id: String
    val title: String
    val path: String
    val size: Long
    val bucketId: String
    val createdAt: Instant
    val updatedAt: Instant
}
