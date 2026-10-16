package com.ismartcoding.plain.features.downloads

import com.ismartcoding.plain.lib.logcat.LogCat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

import com.ismartcoding.plain.features.chat.download.DownloadQueue
import com.ismartcoding.plain.features.shares.SharedFolderDownloadEngine
import com.ismartcoding.plain.platform.system.PlatformLock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object DownloadCenter {
    private val lock = PlatformLock()
    private val state = MutableStateFlow<Map<String, DownloadTaskHandle>>(emptyMap())
    val progress = state.asStateFlow()

    fun replaceExternal(kind: String, items: List<DownloadTaskHandle>) = lock.withLock {
        require(items.all { it.kind == kind })
        state.value = state.value.filterValues { it.kind != kind } + items.associate { it.id to it.flowSnapshot() }
    }

    fun cancel(taskId: String): Boolean = when (progress.value[taskId]?.kind) {
        DOWNLOAD_KIND_SHARE -> SharedFolderDownloadEngine.control(taskId, "cancel")
        DOWNLOAD_KIND_CHAT -> DownloadQueue.removeDownload(taskId)
        else -> false
    }

    fun remove(taskId: String): Boolean = when (progress.value[taskId]?.kind) {
        DOWNLOAD_KIND_SHARE -> SharedFolderDownloadEngine.control(taskId, "remove")
        DOWNLOAD_KIND_CHAT -> DownloadQueue.removeDownload(taskId)
        else -> false
    }
}
