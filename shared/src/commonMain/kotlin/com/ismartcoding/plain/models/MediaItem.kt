package com.ismartcoding.plain.models

import com.ismartcoding.plain.IData
import kotlin.time.Instant

interface MediaItem : IData {
    override var id: String
    val title: String
    val path: String
    val size: Long
    val bucketId: String
    val createdAt: Instant
    val updatedAt: Instant
}
