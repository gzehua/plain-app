package com.ismartcoding.plain.features.videos

import com.ismartcoding.plain.ui.app.AppUiState

import com.ismartcoding.plain.app.TempData
import com.ismartcoding.plain.lib.logcat.LogCat
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days

object VideoProgressHost {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val lock = Mutex()

    suspend fun restore() = lock.withLock {
        val rows = VideoProgressHelper.recentAsync(Clock.System.now() - 30.days)
        withContext(Dispatchers.Main) {
            AppUiState.videoPlayProgressMap.clear()
            rows.forEach { AppUiState.videoPlayProgressMap[it.mediaId] = it.positionMs }
        }
    }

    fun started(mediaId: String, positionMs: Long) {
        if (mediaId.isEmpty() || positionMs < 0) return
        scope.launch(start = CoroutineStart.UNDISPATCHED) {
            lock.withLock {
                try { VideoProgressHelper.recordPlayAsync(mediaId, positionMs) }
                catch (cancelled: CancellationException) { throw cancelled }
                catch (e: Exception) { LogCat.e("Video history: ${e.message}") }
            }
        }
    }

    fun save(mediaId: String, positionMs: Long) {
        if (mediaId.isEmpty() || positionMs < 0) return
        AppUiState.videoPlayProgressMap[mediaId] = positionMs
        scope.launch(start = CoroutineStart.UNDISPATCHED) {
            lock.withLock {
                try {
                    VideoProgressHelper.saveAsync(mediaId, positionMs)
                    withContext(Dispatchers.Main) { AppUiState.videoPlayProgressMap[mediaId] = positionMs }
                }
                catch (cancelled: CancellationException) { throw cancelled }
                catch (e: Exception) { LogCat.e("Video progress: ${e.message}") }
            }
        }
    }
}
