package com.ismartcoding.plain.rust.host

import com.ismartcoding.plain.platform.files.decodeThumbnailBytes
import com.ismartcoding.plain.rust.bridge.RustCoreBridge
import kotlinx.serialization.json.*

internal object ThumbnailHost {
    suspend fun decode(params: JsonObject): JsonElement {
        val path = params.getValue("path").jsonPrimitive.content
        val bytes = decodeThumbnailBytes(path,
            params.getValue("width").jsonPrimitive.int,
            params.getValue("height").jsonPrimitive.int,
            params.getValue("centerCrop").jsonPrimitive.boolean,
            params.getValue("mediaId").jsonPrimitive.content,
            params.getValue("fileName").jsonPrimitive.content,
        ) ?: return JsonNull
        val token = params.getValue("outputToken").jsonPrimitive.content
        val error = RustCoreBridge.send("thumbnail", token, bytes)
        check(error.isEmpty()) { error }
        return JsonPrimitive(true)
    }
}
