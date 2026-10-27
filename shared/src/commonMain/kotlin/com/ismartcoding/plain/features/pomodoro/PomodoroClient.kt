package com.ismartcoding.plain.features.pomodoro

import com.ismartcoding.plain.graphql.Mutation
import com.ismartcoding.plain.graphql.Query
import com.ismartcoding.plain.models.PomodoroToday
import com.ismartcoding.plain.rust.graphql.RustGraphqlApi

object PomodoroClient {
    suspend fun today(): PomodoroToday =
        RustGraphqlApi.execute(Query.pomodoroToday())

    suspend fun start(durationSec: Int): PomodoroToday {
        RustGraphqlApi.execute<Unit>(Mutation.startPomodoro(durationSec))
        return today()
    }

    suspend fun pause(): PomodoroToday {
        RustGraphqlApi.execute<Unit>(Mutation.pausePomodoro())
        return today()
    }

    suspend fun stop(): PomodoroToday {
        RustGraphqlApi.execute<Unit>(Mutation.stopPomodoro())
        return today()
    }

    suspend fun adjust(durationSec: Int): PomodoroToday =
        RustGraphqlApi.execute(Mutation.adjustPomodoro(durationSec))
}
