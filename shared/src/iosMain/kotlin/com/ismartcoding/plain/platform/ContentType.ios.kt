@file:OptIn(ExperimentalForeignApi::class)

package com.ismartcoding.plain.platform

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSFileManager
import platform.UniformTypeIdentifiers.UTType

actual fun getContentTypeForPath(path: String): String? {
    if (path.startsWith("ph://")) {
        val resource = com.ismartcoding.plain.platform.media.IosPhotoResource.resource(path)
        return UTType.typeWithIdentifier(resource.uniformTypeIdentifier)?.preferredMIMEType
    }
    if (!NSFileManager.defaultManager.fileExistsAtPath(path)) return null
    val ext = path.substringAfterLast('.', "").lowercase()
    if (ext.isEmpty()) return null
    val type = UTType.typeWithFilenameExtension(ext) ?: return null
    return type.preferredMIMEType
}
