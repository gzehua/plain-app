package com.ismartcoding.plain.graphql.models

import com.ismartcoding.plain.ui.page.pomodoro.PomodoroState
import kotlinx.serialization.Serializable

@Serializable
data class PomodoroTickResult(
    var today: PomodoroToday,
    var completedState: PomodoroState?,
)
