package com.ismartcoding.plain.features.settings

import com.ismartcoding.plain.api.graphql.models.AppUpdateDownload
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.platform.system.Capability
import com.ismartcoding.plain.platform.system.getDeviceCapabilities
import com.ismartcoding.plain.rust.PlainRust
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

object AppUpdateDownloads {
    private const val FIELDS = "coreId taskId revision status progress filePath error"
    private val lock = Mutex()
    private val snapshot = MutableStateFlow(AppUpdateDownload())
    val state = snapshot.asStateFlow()

    suspend fun apply(value: AppUpdateDownload) = lock.withLock {
        if (value.coreId != snapshot.value.coreId || value.revision >= snapshot.value.revision) snapshot.value = value
    }

    suspend fun refresh() {
        if (Capability.APP_UPDATES !in getDeviceCapabilities()) return
        apply(JsonHelper.jsonDecodeFromElement(PlainRust.mainQuery("appUpdateDownload { $FIELDS }").getValue("appUpdateDownload")))
    }

    suspend fun start() {
        apply(JsonHelper.jsonDecodeFromElement(PlainRust.mainMutate("startAppUpdateDownload { $FIELDS }").getValue("startAppUpdateDownload")))
    }

    suspend fun cancel() {
        apply(JsonHelper.jsonDecodeFromElement(PlainRust.mainMutate("cancelAppUpdateDownload { $FIELDS }").getValue("cancelAppUpdateDownload")))
    }
}
