package com.ismartcoding.plain.graphql.models

import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
data class PomodoroRecord(
    var id: String,
    var date: String,
    var completedCount: Int,
    var totalWorkSec: Long,
    var totalBreakSec: Long,
    var createdAt: Instant,
    var updatedAt: Instant,
)
