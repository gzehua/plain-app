package com.ismartcoding.plain.platform

import kotlinx.cinterop.ExperimentalForeignApi
import platform.UniformTypeIdentifiers.UTType

@OptIn(ExperimentalForeignApi::class)
actual fun getMimeTypeFromExtension(extension: String): String {
    val ext = extension.lowercase()
    val common = CommonMimeTypes[ext]
    if (common.isNotEmpty()) return common
    return try {
        val type = UTType.typeWithFilenameExtension(ext)
        if (type != null) {
            type.preferredMIMEType ?: ""
        } else {
            ""
        }
    } catch (e: Exception) {
        ""
    }
}
