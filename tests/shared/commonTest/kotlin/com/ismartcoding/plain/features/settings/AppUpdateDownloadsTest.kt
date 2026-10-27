package com.ismartcoding.plain.features.settings

import com.ismartcoding.plain.features.settings.AppUpdateDownload
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class AppUpdateDownloadsTest {
    @Test
    fun delayedCommandRepliesCannotReplaceNewerTaskEvents() = runTest {
        AppUpdateDownloads.apply(AppUpdateDownload(coreId = "projection-order", revision = 10, taskId = "new", status = AppUpdateDownloadStatus.COMPLETED, progress = 100, filePath = "/cache/package"))
        AppUpdateDownloads.apply(AppUpdateDownload(coreId = "projection-order", revision = 9, taskId = "old", status = AppUpdateDownloadStatus.DOWNLOADING, progress = 20))
        assertEquals("new", AppUpdateDownloads.state.value.taskId)
        assertEquals(AppUpdateDownloadStatus.COMPLETED, AppUpdateDownloads.state.value.status)
        assertEquals(100, AppUpdateDownloads.state.value.progress)
    }

    @Test
    fun restartedCoreSnapshotAcceptsItsNewRevisionSequence() = runTest {
        AppUpdateDownloads.apply(AppUpdateDownload(coreId = "before-restart", revision = 100, status = AppUpdateDownloadStatus.DOWNLOADING))
        AppUpdateDownloads.apply(AppUpdateDownload(coreId = "after-restart", revision = 1, status = AppUpdateDownloadStatus.IDLE))
        assertEquals("after-restart", AppUpdateDownloads.state.value.coreId)
        assertEquals(AppUpdateDownloadStatus.IDLE, AppUpdateDownloads.state.value.status)
    }
}
