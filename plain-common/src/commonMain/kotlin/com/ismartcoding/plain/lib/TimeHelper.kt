package com.ismartcoding.plain.lib

import kotlin.time.Clock
import kotlin.time.Instant

object TimeHelper {
    fun now(): Instant = Clock.System.now()
    fun nowMillis(): Long = Clock.System.now().toEpochMilliseconds()
}