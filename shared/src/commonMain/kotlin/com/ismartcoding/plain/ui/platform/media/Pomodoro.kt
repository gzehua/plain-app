package com.ismartcoding.plain.ui.platform.media

import com.ismartcoding.plain.models.PomodoroSettings
import com.ismartcoding.plain.models.PomodoroState

expect suspend fun showPomodoroNotification(state: PomodoroState, nextState: PomodoroState)

expect suspend fun playPomodoroCompletionSound(settings: PomodoroSettings)
