package com.ismartcoding.plain.models

import kotlinx.serialization.Serializable

@Serializable
data class PomodoroSettings(
    var workDurationMin: Int,
    var shortBreakDurationMin: Int,
    var longBreakDurationMin: Int,
    var pomodorosBeforeLongBreak: Int,
    var showNotification: Boolean,
    var playSoundOnComplete: Boolean,
    var soundPath: String,
    var originalSoundName: String,
)
