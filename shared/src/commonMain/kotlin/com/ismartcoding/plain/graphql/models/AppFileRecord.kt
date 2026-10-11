package com.ismartcoding.plain.graphql.models

import com.ismartcoding.plain.lib.TimeHelper
import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
data class AppFileRecord(
    var id: String,
    var fileName: String = "",
    @kotlinx.serialization.Required var size: Long = 0L,
    @kotlinx.serialization.Required var mimeType: String = "",
    @kotlinx.serialization.Required var realPath: String = "",
    @kotlinx.serialization.Required var refCount: Int = 1,
    @kotlinx.serialization.Required var weakHash: String = "",
    @kotlinx.serialization.Required var createdAt: Instant = TimeHelper.now(),
    @kotlinx.serialization.Required var updatedAt: Instant = TimeHelper.now(),
) {
    fun getFidUri(): String {
        val fileName = realPath.substringAfterLast('/', "")
        return "fid:$fileName"
    }
}
