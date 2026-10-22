package com.ismartcoding.plain.ui.platform.media

import com.ismartcoding.plain.models.PomodoroSettings
import com.ismartcoding.plain.models.PomodoroState

actual suspend fun showPomodoroNotification(state: PomodoroState, nextState: PomodoroState) {
}

actual suspend fun playPomodoroCompletionSound(settings: PomodoroSettings) {
}
