package com.ismartcoding.plain.platform

import com.ismartcoding.plain.graphql.models.PomodoroSettings
import com.ismartcoding.plain.ui.page.pomodoro.PomodoroState

expect suspend fun showPomodoroNotification(state: PomodoroState, nextState: PomodoroState)

expect suspend fun playPomodoroCompletionSound(settings: PomodoroSettings)
