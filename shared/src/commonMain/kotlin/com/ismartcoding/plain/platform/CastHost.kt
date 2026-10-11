package com.ismartcoding.plain.platform

import com.ismartcoding.plain.lib.JsonHelper
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

internal object CastHost {
    suspend fun handle(method: String, params: JsonObject): JsonElement = when (method) {
        "systemCastAddressFacts" -> JsonHelper.jsonEncodeToElement(CastAddressFacts(
            getDeviceIP4(),
            getDeviceName(),
        ))
        else -> error("Unsupported cast platform operation")
    }
}
