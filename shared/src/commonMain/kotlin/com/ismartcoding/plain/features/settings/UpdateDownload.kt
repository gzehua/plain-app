package com.ismartcoding.plain.features.settings

import com.ismartcoding.plain.rust.native.RustNativeApi


import com.ismartcoding.plain.features.settings.AppUpdateDownload
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

object AppUpdateDownloads {
    private val lock = Mutex()
    private val snapshot = MutableStateFlow(AppUpdateDownload())
    val state = snapshot.asStateFlow()

    suspend fun apply(value: AppUpdateDownload) = lock.withLock {
        if (value.coreId != snapshot.value.coreId || value.revision >= snapshot.value.revision) snapshot.value = value
    }

    suspend fun start() {
        apply(RustNativeApi.startAppUpdateDownload())
    }

    suspend fun cancel() {
        apply(RustNativeApi.cancelAppUpdateDownload())
    }
}
