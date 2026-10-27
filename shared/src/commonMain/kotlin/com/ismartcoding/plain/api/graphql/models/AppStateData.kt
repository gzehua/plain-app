package com.ismartcoding.plain.api.graphql.models

import com.ismartcoding.plain.models.PomodoroToday
import kotlinx.serialization.Serializable

@Serializable
internal data class PomodoroAction(val today: PomodoroToday, val completedState: String? = null)
