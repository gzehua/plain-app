package com.ismartcoding.plain.features.settings

import com.ismartcoding.plain.rust.native.RustNativeApi


object UpdatesClient {
    suspend fun check(automatic: Boolean = false): String =
        RustNativeApi.checkAppUpdate(automatic)
}
