package com.ismartcoding.plain.models

import kotlinx.serialization.Serializable

@Serializable
enum class PomodoroState {
    WORK, SHORT_BREAK, LONG_BREAK,
}
