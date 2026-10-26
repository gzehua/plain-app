package com.ismartcoding.plain.platform.files

import com.ismartcoding.plain.features.media.actions.MediaProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonElement

suspend fun handleMediaProvider(method: String, params: JsonObject): JsonElement = withContext(Dispatchers.IO) { MediaProvider.handle(method, params) }
