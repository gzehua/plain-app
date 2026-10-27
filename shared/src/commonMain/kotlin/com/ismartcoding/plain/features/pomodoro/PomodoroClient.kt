package com.ismartcoding.plain.features.pomodoro

import com.ismartcoding.plain.graphql.Mutation
import com.ismartcoding.plain.graphql.Query
import com.ismartcoding.plain.models.PomodoroToday
import com.ismartcoding.plain.rust.graphql.RustGraphqlApi

object PomodoroClient {
    suspend fun today(): PomodoroToday =
        RustGraphqlApi.read(Query.pomodoroToday())

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

    suspend fun adjust(durationSec: Int): PomodoroToday =
        RustGraphqlApi.read(Mutation.adjustPomodoro(durationSec))
}
