package com.ismartcoding.plain.features.pomodoro

import com.ismartcoding.plain.graphql.Mutation
import com.ismartcoding.plain.graphql.Query
import com.ismartcoding.plain.rust.graphql.RustGraphqlApi
import com.ismartcoding.plain.models.PomodoroToday
import kotlinx.serialization.json.*
import com.ismartcoding.plain.lib.JsonHelper

object PomodoroClient {
    suspend fun today(): PomodoroToday {
        return RustGraphqlApi.graphql(Query.pomodoroToday()).getValue("pomodoroToday").today()
    }
    suspend fun start(durationSec: Int): PomodoroToday {
        RustGraphqlApi.graphql(Mutation.startPomodoro(durationSec))
        return today()
    }
    suspend fun pause(): PomodoroToday {
        RustGraphqlApi.graphql(Mutation.pausePomodoro())
        return today()
    }
    suspend fun stop(): PomodoroToday {
        RustGraphqlApi.graphql(Mutation.stopPomodoro())
        return today()
    }
    suspend fun adjust(durationSec: Int): PomodoroToday = RustGraphqlApi.graphql(Mutation.adjustPomodoro(durationSec))
        .getValue("adjustPomodoro").today()
    internal fun JsonElement.today(): PomodoroToday =
        JsonHelper.jsonDecodeFromElement(this)
}
