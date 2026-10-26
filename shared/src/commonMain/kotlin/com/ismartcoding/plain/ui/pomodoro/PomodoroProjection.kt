package com.ismartcoding.plain.ui.pomodoro

import kotlinx.serialization.Serializable
import com.ismartcoding.plain.models.PomodoroToday

import com.ismartcoding.plain.models.PomodoroState

import com.ismartcoding.plain.events.HPomodoroChangedEvent
import com.ismartcoding.plain.features.pomodoro.PomodoroClient.today
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.lib.logcat.LogCat
import com.ismartcoding.plain.lib.sendEvent
import com.ismartcoding.plain.Prefs
import com.ismartcoding.plain.ui.platform.media.playPomodoroCompletionSound
import com.ismartcoding.plain.ui.platform.media.showPomodoroNotification
import kotlinx.coroutines.CancellationException

internal object PomodoroProjection {
    suspend fun refresh() {
        sendEvent(HPomodoroChangedEvent(today()))
    }

    suspend fun apply(payload: String) {
        val event = JsonHelper.jsonDecode<PomodoroAction>(payload)
        val today = event.today
        sendEvent(HPomodoroChangedEvent(today))
        val completed = event.completedState ?: return
        val state = PomodoroState.valueOf(completed)
        try {
            showPomodoroNotification(state, today.state)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            LogCat.e("Pomodoro notification", error)
        }
        try {
            playPomodoroCompletionSound(Prefs.pomodoroSettings.value)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            LogCat.e("Pomodoro completion sound", error)
        }
    }
}

@Serializable
private data class PomodoroAction(val today: PomodoroToday, val completedState: String? = null)
