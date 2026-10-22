package com.ismartcoding.plain.models

import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
data class AndroidExtras(
    var sdkVersion: Int,
    var versionCodeName: String,
    var securityPatch: String,
    var bootloader: String,
    var fingerprint: String,
    var hardware: String,
    var radioVersion: String,
    var board: String,
    var buildBrand: String,
    var buildNumber: String,
    var device: String,
    var javaVmVersion: String,
    var glEsVersion: String,
    var buildTime: Instant,
)
