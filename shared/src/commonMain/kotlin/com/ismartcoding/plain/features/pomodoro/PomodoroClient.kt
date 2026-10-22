package com.ismartcoding.plain.features.pomodoro

import com.ismartcoding.plain.rust.PlainRust
import com.ismartcoding.plain.models.PomodoroToday
import kotlinx.serialization.json.*

object PomodoroClient {
    private const val FIELDS = "date completedCount currentRound timeLeftSec totalTimeSec isRunning isPaused state"
    suspend fun today(): PomodoroToday {
        return PlainRust.mainQuery("pomodoroToday { $FIELDS }").getValue("pomodoroToday").today()
    }
    suspend fun start(durationSec: Int): PomodoroToday {
        PlainRust.mainMutate("startPomodoro(durationSec: $durationSec)")
        return today()
    }
    suspend fun pause(): PomodoroToday {
        PlainRust.mainMutate("pausePomodoro")
        return today()
    }
    suspend fun stop(): PomodoroToday {
        PlainRust.mainMutate("stopPomodoro")
        return today()
    }
    suspend fun adjust(durationSec: Int): PomodoroToday = PlainRust.mainMutate("adjustPomodoro(durationSec: $durationSec) { $FIELDS }")
        .getValue("adjustPomodoro").today()
    internal fun JsonElement.today(): PomodoroToday =
        com.ismartcoding.plain.lib.JsonHelper.jsonDecodeFromElement(this)
}
