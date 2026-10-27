package com.ismartcoding.plain.models

import kotlinx.serialization.Serializable

@Serializable
internal data class PomodoroAction(val today: PomodoroToday, val completedState: String? = null)
