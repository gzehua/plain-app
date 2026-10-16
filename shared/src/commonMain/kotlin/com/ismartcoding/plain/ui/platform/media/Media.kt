package com.ismartcoding.plain.ui.platform.media

import com.ismartcoding.plain.platform.files.renameAndScanFile

import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.long
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.IntSize
import com.ismartcoding.plain.api.graphql.models.ImageFileInfo
import com.ismartcoding.plain.api.graphql.models.VideoFileInfo

suspend fun getVideoMeta(path: String): VideoFileInfo? {
    val info = com.ismartcoding.plain.rust.PlainRust.mainGraphql(
        "query(\$path:String!,\$name:String!){fileInfo(path:\$path,fileName:\$name){data{__typename ... on VideoFileInfo{width height rotation displayWidth displayHeight durationMs bitrate frameRate title artist album genre takenAt writer composer location{latitude longitude}}}}}",
        mapOf("path" to kotlinx.serialization.json.JsonPrimitive(path), "name" to kotlinx.serialization.json.JsonPrimitive("video.mp4")),
    ).getValue("fileInfo").jsonObject.getValue("data")
    return if (info is kotlinx.serialization.json.JsonNull) null else com.ismartcoding.plain.lib.JsonHelper.jsonDecodeFromElement(info)
}

suspend fun getImageMeta(path: String): ImageFileInfo? {
    val info = com.ismartcoding.plain.rust.PlainRust.mainGraphql(
        "query(\$path:String!,\$name:String!){fileInfo(path:\$path,fileName:\$name){data{__typename ... on ImageFileInfo{width height rotation make model colorSpace apertureValue exposureTime focalLength isoSpeed takenAt flash fNumber exposureProgram meteringMode whiteBalance creator resolutionX resolutionY description location{latitude longitude}}}}}",
        mapOf("path" to kotlinx.serialization.json.JsonPrimitive(path), "name" to kotlinx.serialization.json.JsonPrimitive("image.jpg")),
    ).getValue("fileInfo").jsonObject.getValue("data")
    return if (info is kotlinx.serialization.json.JsonNull) null else com.ismartcoding.plain.lib.JsonHelper.jsonDecodeFromElement(info)
}

/**
 * Try to decode a QR code from the image at [path]. Returns the decoded text, or null if
 * no QR code is found (or decoding is not supported on the platform).
 */
expect fun tryDecodeQrCode(path: String): String?

/**
 * Returns the size in bytes of the file at [path], or 0L if the file does not exist.
 */
expect fun fileLength(path: String): Long

/**
 * Rename a media file from [path] to a name derived from [newName] (same directory).
 * Returns the new absolute path on success, or null on failure.
 * On Android this also triggers a MediaScanner scan so the gallery stays in sync.
 */
suspend fun renameMediaFile(path: String, newName: String): String? = renameAndScanFile(path, newName)

suspend fun getMediaDurationMs(path: String): Long = com.ismartcoding.plain.rust.PlainRust.mainQuery(
    "mediaDurationMs(path: ${com.ismartcoding.plain.rust.request.gql(path)})"
).getValue("mediaDurationMs").jsonPrimitive.long

suspend fun getAudioDurationMs(path: String): Long = getMediaDurationMs(path)

expect fun generateQrCode(text: String, width: Int, height: Int): ImageBitmap

/**
 * Returns the intrinsic (width, height) of the SVG file at [path], or a default
 * 150x150 size on failure / unsupported platforms.
 */
expect fun getSvgSize(path: String): IntSize

/**
 * Pin a media shortcut to the home screen (Android) / do nothing (iOS).
 * [path] is the media file path, [label] is the shortcut display name.
 */
expect fun addMediaShortcut(path: String, label: String)
