package com.ismartcoding.plain.models

import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlinx.serialization.SerialName

@SerialName("ImageFileInfo")
@Serializable
data class ImageFileInfo(
    var width: Int,
    var height: Int,
    var location: Location?,
    val make: String = "",
    val model: String = "",
    val rotation: Int = 0,
    val colorSpace: String = "",
    val apertureValue: Double = 0.0,
    val exposureTime: String = "",
    val focalLength: String = "",
    val isoSpeed: Int = 0,
    val takenAt: Instant? = null,
    val flash: Int = 0,
    val fNumber: Double = 0.0,
    val exposureProgram: Int = 0,
    val meteringMode: Int = 0,
    val whiteBalance: Int = 0,
    val creator: String = "",
    val resolutionX: Int = 0,
    val resolutionY: Int = 0,
    val description: String = "",

) : MediaFileInfo {
    val isScreenshot: Boolean get() = exposureTime.isEmpty()
}
