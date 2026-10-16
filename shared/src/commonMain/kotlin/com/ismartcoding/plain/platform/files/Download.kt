package com.ismartcoding.plain.platform.files

import com.ismartcoding.plain.rust.http.get

expect fun getMimeTypeFromExtension(extension: String): String

internal object CommonMimeTypes {
    operator fun get(extension: String): String = when (extension.lowercase()) {
        "txt" -> "text/plain"
        "pdf" -> "application/pdf"
        "jpg", "jpeg" -> "image/jpeg"
        "png" -> "image/png"
        "gif" -> "image/gif"
        "mp4" -> "video/mp4"
        "mp3" -> "audio/mpeg"
        "zip" -> "application/zip"
        else -> ""
    }
}
