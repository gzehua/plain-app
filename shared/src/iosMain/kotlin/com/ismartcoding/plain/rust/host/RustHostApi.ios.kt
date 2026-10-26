package com.ismartcoding.plain.rust.host

import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.json.JsonObject

internal actual suspend fun executePlatformHostRequest(method: String, params: JsonObject, scope: CoroutineScope): HostResult =
    error("Unknown Rust host operation: $method")
