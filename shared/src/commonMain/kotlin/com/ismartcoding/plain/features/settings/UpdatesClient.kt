package com.ismartcoding.plain.features.settings

import com.ismartcoding.plain.rust.PlainRust
import kotlinx.serialization.json.jsonPrimitive

object UpdatesClient {
    suspend fun check(automatic: Boolean = false): String =
        PlainRust.mainMutate("checkAppUpdate(automatic: $automatic)").getValue("checkAppUpdate").jsonPrimitive.content
}
