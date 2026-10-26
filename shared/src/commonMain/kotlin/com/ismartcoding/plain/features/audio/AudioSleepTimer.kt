package com.ismartcoding.plain.features.audio

import androidx.compose.runtime.mutableLongStateOf
import com.ismartcoding.plain.lib.TimeHelper
import com.ismartcoding.plain.lib.coMain
import com.ismartcoding.plain.platform.media.audioPause
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay

object AudioSleepTimer {
    val durationMs = mutableLongStateOf(0L)
    val endsAt = mutableLongStateOf(0L)
    private var job: Job? = null

    fun start(durationMs: Long) {
        cancel()
        this.durationMs.longValue = durationMs
        endsAt.longValue = TimeHelper.nowMillis() + durationMs
        job = coMain {
            delay(durationMs)
            endsAt.longValue = 0
            job = null
            audioPause()
        }
    }

    fun cancel() {
        job?.cancel()
        job = null
        endsAt.longValue = 0
    }
}
