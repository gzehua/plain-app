package com.ismartcoding.plain.db

data class DAudioQueueItem(
    val mediaId: String,
    val path: String,
    var sortOrder: Int,
    var title: String,
    var artist: String,
    var durationMs: Long,
)
