package com.ismartcoding.plain.ui.pomodoro

import com.ismartcoding.plain.api.graphql.models.PomodoroState

import com.ismartcoding.plain.app.events.HPomodoroChangedEvent
import com.ismartcoding.plain.features.pomodoro.PomodoroClient
import com.ismartcoding.plain.features.pomodoro.PomodoroClient.today
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.lib.logcat.LogCat
import com.ismartcoding.plain.lib.sendEvent
import com.ismartcoding.plain.preferences.UserPrefs
import com.ismartcoding.plain.ui.platform.media.playPomodoroCompletionSound
import com.ismartcoding.plain.ui.platform.media.showPomodoroNotification
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.*

internal object PomodoroProjection {
    suspend fun refresh() {
        sendEvent(HPomodoroChangedEvent(PomodoroClient.today()))
    }

    suspend fun apply(payload: String) {
        val event = JsonHelper.jsonDecode<JsonObject>(payload)
        val today = event.getValue("today").today()
        sendEvent(HPomodoroChangedEvent(today))
        val completed = event["completedState"]?.jsonPrimitive?.contentOrNull ?: return
        val state = PomodoroState.valueOf(completed)
        try {
            showPomodoroNotification(state, today.state)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            LogCat.e("Pomodoro notification", error)
        }
        try {
            playPomodoroCompletionSound(UserPrefs.pomodoroSettings.value)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            LogCat.e("Pomodoro completion sound", error)
        }
    }
}
