package com.ismartcoding.plain.features.settings

import com.ismartcoding.plain.rust.PlainRust

object UpdatesClient {
    suspend fun check(automatic: Boolean = false): String =
        PlainRust.checkAppUpdate(automatic)
}
