package com.ismartcoding.plain.ui.platform.media

import com.ismartcoding.plain.i18n.*
import com.ismartcoding.plain.models.PomodoroSettings
import com.ismartcoding.plain.models.PomodoroState
import com.ismartcoding.plain.Prefs
import com.ismartcoding.plain.ui.extensions.getFinalPath
import com.ismartcoding.plain.ui.i18n.LocaleHelper

suspend fun showPomodoroNotification(state: PomodoroState, nextState: PomodoroState) {
    if (!Prefs.pomodoroSettings.value.showNotification) return
    val (title, message) = when (state) {
        PomodoroState.WORK -> {
            val messageRes = if (nextState == PomodoroState.LONG_BREAK) Res.string.great_job_long_break else Res.string.great_job_short_break
            LocaleHelper.getStringAsync(Res.string.work_session_complete) to LocaleHelper.getStringAsync(messageRes)
        }
        PomodoroState.SHORT_BREAK -> LocaleHelper.getStringAsync(Res.string.break_complete) to LocaleHelper.getStringAsync(Res.string.time_to_work)
        PomodoroState.LONG_BREAK -> LocaleHelper.getStringAsync(Res.string.long_break_complete) to LocaleHelper.getStringAsync(Res.string.ready_for_work)
    }
    postPomodoroNotification(title, message)
}

suspend fun playPomodoroCompletionSound(settings: PomodoroSettings) {
    if (!settings.playSoundOnComplete) return
    val path = if (settings.soundPath.isEmpty()) null else settings.soundPath.getFinalPath()
    playPomodoroSound(path)
}

internal expect suspend fun postPomodoroNotification(title: String, message: String)

internal expect suspend fun playPomodoroSound(path: String?)
