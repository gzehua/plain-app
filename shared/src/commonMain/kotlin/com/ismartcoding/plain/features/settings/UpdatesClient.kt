package com.ismartcoding.plain.features.settings

import com.ismartcoding.plain.rust.request.NativeManagementClient

object UpdatesClient {
    suspend fun check(automatic: Boolean = false): String =
        NativeManagementClient.checkAppUpdate(automatic)
}
