package com.ismartcoding.plain.features.pomodoro

import com.ismartcoding.plain.rust.graphql.RustGraphqlApi

import com.ismartcoding.plain.models.PomodoroToday
import kotlinx.serialization.json.*

object PomodoroClient {
    private const val FIELDS = "date completedCount currentRound timeLeftSec totalTimeSec isRunning isPaused state"
    suspend fun today(): PomodoroToday {
        return RustGraphqlApi.query("pomodoroToday { $FIELDS }").getValue("pomodoroToday").today()
    }
    suspend fun start(durationSec: Int): PomodoroToday {
        RustGraphqlApi.mutate("startPomodoro(durationSec: $durationSec)")
        return today()
    }
    suspend fun pause(): PomodoroToday {
        RustGraphqlApi.mutate("pausePomodoro")
        return today()
    }
    suspend fun stop(): PomodoroToday {
        RustGraphqlApi.mutate("stopPomodoro")
        return today()
    }
    suspend fun adjust(durationSec: Int): PomodoroToday = RustGraphqlApi.mutate("adjustPomodoro(durationSec: $durationSec) { $FIELDS }")
        .getValue("adjustPomodoro").today()
    internal fun JsonElement.today(): PomodoroToday =
        com.ismartcoding.plain.lib.JsonHelper.jsonDecodeFromElement(this)
}
