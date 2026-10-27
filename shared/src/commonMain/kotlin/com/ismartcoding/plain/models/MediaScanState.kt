package com.ismartcoding.plain.models

import kotlinx.serialization.Serializable

@Serializable
enum class MediaScanState {
    IDLE,
    RUNNING,
    PAUSED,
    STOPPED,
    FAILED,
}
