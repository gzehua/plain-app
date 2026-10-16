package com.ismartcoding.plain.api.graphql.models

import com.ismartcoding.plain.api.graphql.models.PomodoroState
import kotlinx.serialization.Serializable

@Serializable
data class PomodoroTickResult(
    var today: PomodoroToday,
    var completedState: PomodoroState?,
)
