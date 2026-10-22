package com.ismartcoding.plain.models

import com.ismartcoding.plain.platform.system.DevicePlatform
import kotlinx.serialization.Serializable

@Serializable
data class DeviceInfo(
    var name: String,
    var platform: DevicePlatform,
    var manufacturer: String,
    var model: String,
    var osName: String,
    var osVersion: String,
    var kernelVersion: String,
    var appVersion: String,
    var appBuildNumber: String,
    var language: String,
    var cpuArch: String,
    var cpuModel: String?,
    var totalMemory: Long,
    var totalStorage: Long,
    var display: DisplayInfo?,
    var android: AndroidExtras?,
)
