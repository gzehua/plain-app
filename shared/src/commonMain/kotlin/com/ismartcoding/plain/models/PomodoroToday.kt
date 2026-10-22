package com.ismartcoding.plain.models

import com.ismartcoding.plain.models.PomodoroState
import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
data class PomodoroToday(
    var date: Instant,
    var completedCount: Int,
    var currentRound: Int,
    var timeLeftSec: Int,
    var totalTimeSec: Int,
    var isRunning: Boolean,
    var isPaused: Boolean,
    var state: PomodoroState,
)
