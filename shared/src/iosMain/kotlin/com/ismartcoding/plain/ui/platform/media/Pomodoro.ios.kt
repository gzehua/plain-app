package com.ismartcoding.plain.ui.platform.media

import com.ismartcoding.plain.api.graphql.models.PomodoroSettings
import com.ismartcoding.plain.ui.pomodoro.PomodoroState

actual suspend fun showPomodoroNotification(state: PomodoroState, nextState: PomodoroState) {
}

actual suspend fun playPomodoroCompletionSound(settings: PomodoroSettings) {
}
