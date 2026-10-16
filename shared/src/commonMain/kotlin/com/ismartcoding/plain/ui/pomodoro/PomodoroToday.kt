package com.ismartcoding.plain.ui.pomodoro

import com.ismartcoding.plain.ui.pomodoro.PomodoroState
import kotlin.time.Instant

data class PomodoroToday(
    val date: Instant,
    val completedCount: Int,
    val currentRound: Int,
    val timeLeftSec: Int,
    val totalTimeSec: Int,
    val isRunning: Boolean,
    val isPaused: Boolean,
    val state: PomodoroState,
)
