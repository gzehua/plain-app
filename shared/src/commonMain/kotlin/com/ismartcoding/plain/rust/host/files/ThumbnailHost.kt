package com.ismartcoding.plain.rust.host.files

import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.platform.files.decodeThumbnailBytes
import com.ismartcoding.plain.rust.host.*
import com.ismartcoding.plain.rust.native.RustNativeBindings
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

internal object ThumbnailHost {
    internal suspend fun decode(params: JsonObject): HostResult {
        val request = JsonHelper.jsonDecodeFromElement<ThumbnailRequest>(params)
        val bytes = decodeThumbnailBytes(request.path,
            request.width,
            request.height,
            request.centerCrop,
            request.mediaId,
            request.fileName,
        ) ?: return hostResult()
        val token = request.outputToken
        val error = RustNativeBindings.submitThumbnail(token, bytes)
        check(error.isEmpty()) { error }
        return hostResult(true)
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
