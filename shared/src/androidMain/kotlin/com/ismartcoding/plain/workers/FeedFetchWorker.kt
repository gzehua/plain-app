package com.ismartcoding.plain.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ismartcoding.plain.lib.TimeHelper
import com.ismartcoding.plain.platform.system.BackgroundExecution

class FeedFetchWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        BackgroundExecution.run(TimeHelper.nowMillis() + 9 * 60 * 1000L)
        return Result.success()
    }
}
