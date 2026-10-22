package com.ismartcoding.plain.rust.host

import com.ismartcoding.plain.lib.JsonHelper
import kotlinx.serialization.Serializable

import com.ismartcoding.plain.platform.files.decodeThumbnailBytes
import com.ismartcoding.plain.rust.native.RustNativeApi
import kotlinx.serialization.json.*

internal object ThumbnailHost {
    suspend fun decode(params: JsonObject): JsonElement {
        val request = JsonHelper.jsonDecodeFromElement<ThumbnailRequest>(params)
        val bytes = decodeThumbnailBytes(request.path,
            request.width,
            request.height,
            request.centerCrop,
            request.mediaId,
            request.fileName,
        ) ?: return JsonNull
        val token = request.outputToken
        val error = RustNativeApi.send("thumbnail", token, bytes)
        check(error.isEmpty()) { error }
        return JsonPrimitive(true)
    }
}

@Serializable
private data class ThumbnailRequest(
    val path: String,
    val width: Int,
    val height: Int,
    val centerCrop: Boolean,
    val mediaId: String,
    val fileName: String,
    val outputToken: String,
)
