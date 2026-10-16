package com.ismartcoding.plain.platform

import com.ismartcoding.plain.graphql.models.PomodoroSettings
import com.ismartcoding.plain.ui.page.pomodoro.PomodoroState

actual suspend fun showPomodoroNotification(state: PomodoroState, nextState: PomodoroState) {
}

actual suspend fun playPomodoroCompletionSound(settings: PomodoroSettings) {
}
