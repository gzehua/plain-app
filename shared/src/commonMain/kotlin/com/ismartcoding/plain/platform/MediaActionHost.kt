package com.ismartcoding.plain.platform

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

expect suspend fun handleMediaProvider(method: String, params: JsonObject): JsonElement
