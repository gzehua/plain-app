package com.ismartcoding.plain.api.graphql.models

import com.ismartcoding.plain.features.files.FileTaskStatus
import com.ismartcoding.plain.features.files.FileTaskType
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
