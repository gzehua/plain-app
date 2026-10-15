package com.ismartcoding.plain.platform

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonElement

actual suspend fun handleMediaProvider(method: String, params: JsonObject): JsonElement = error("System media actions are unavailable on iOS")
