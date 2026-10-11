package com.ismartcoding.plain.graphql.models

import com.ismartcoding.plain.features.file.FileTaskStatus
import com.ismartcoding.plain.features.file.FileTaskType
import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
data class FileHostTask(
    var id: String,
    var clientId: String,
    var type: FileTaskType,
    var title: String,
    var status: FileTaskStatus,
    var error: String,
    var totalBytes: Long,
    var doneBytes: Long,
    var totalItems: Long,
    var doneItems: Long,
    var createdAt: Instant,
    var updatedAt: Instant,
    var completedOps: List<FileHostCompletedOp>,
)
