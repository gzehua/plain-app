package com.ismartcoding.plain.platform.system

import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.ismartcoding.plain.appContext
import com.ismartcoding.plain.lib.TimeHelper
import com.ismartcoding.plain.lib.withIO
import com.ismartcoding.plain.workers.FeedFetchWorker
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private const val WAKE_TAG = "system.background.wake"
private val wakeLock = Mutex()

internal actual suspend fun scheduleBackgroundWake(atMs: Long?) = wakeLock.withLock {
    withIO {
        val manager = WorkManager.getInstance(appContext)
        manager.getWorkInfosByTag(WAKE_TAG).get().filter { it.state == WorkInfo.State.ENQUEUED || it.state == WorkInfo.State.BLOCKED }.forEach { manager.cancelWorkById(it.id).result.get() }
        if (atMs != null) {
            manager.enqueue(OneTimeWorkRequestBuilder<FeedFetchWorker>()
                .setInitialDelay((atMs - TimeHelper.nowMillis()).coerceAtLeast(0), TimeUnit.MILLISECONDS)
                .addTag(WAKE_TAG).build()).result.get()
        }
    }
}
