package com.ismartcoding.plain.platform

import com.ismartcoding.plain.features.mediaactions.MediaProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonElement

actual suspend fun handleMediaProvider(method: String, params: JsonObject): JsonElement = withContext(Dispatchers.IO) { MediaProvider.handle(method, params) }
