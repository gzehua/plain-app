package com.ismartcoding.plain.features.pomodoro

import com.ismartcoding.plain.rust.request.RustContentApi
import com.ismartcoding.plain.api.graphql.models.PomodoroToday
import kotlinx.serialization.json.*

object PomodoroClient {
    private const val FIELDS = "date completedCount currentRound timeLeftSec totalTimeSec isRunning isPaused state"
    suspend fun today(): PomodoroToday {
        return RustContentApi.query("pomodoroToday { $FIELDS }").getValue("pomodoroToday").today()
    }
    suspend fun start(durationSec: Int): PomodoroToday {
        RustContentApi.mutate("startPomodoro(durationSec: $durationSec)")
        return today()
    }
    suspend fun pause(): PomodoroToday {
        RustContentApi.mutate("pausePomodoro")
        return today()
    }
    suspend fun stop(): PomodoroToday {
        RustContentApi.mutate("stopPomodoro")
        return today()
    }
    suspend fun adjust(durationSec: Int): PomodoroToday = RustContentApi.mutate("adjustPomodoro(durationSec: $durationSec) { $FIELDS }")
        .getValue("adjustPomodoro").today()
    internal fun JsonElement.today(): PomodoroToday =
        com.ismartcoding.plain.lib.JsonHelper.jsonDecodeFromElement(this)
}
