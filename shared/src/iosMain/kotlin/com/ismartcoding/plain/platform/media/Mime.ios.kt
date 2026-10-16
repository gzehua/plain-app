package com.ismartcoding.plain.platform.media

import platform.UniformTypeIdentifiers.UTType

actual fun getExtensionFromMimeType(mimeType: String): String {
    val type = UTType.typeWithMIMEType(mimeType)
    if (type != null) {
        val ext = type.preferredFilenameExtension
        if (ext != null) {
            return ext
        }
    }
    return ""
}
