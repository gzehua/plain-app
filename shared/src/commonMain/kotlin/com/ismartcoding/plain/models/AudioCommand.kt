package com.ismartcoding.plain.models

import kotlinx.serialization.Serializable

@Serializable
enum class AudioCommand {
    PLAY,
    PAUSE,
    SEEK,
    NEXT,
    PREVIOUS,
    COMPLETED,
    CLEAR,
    RESTART,
    SET_SPEED,
}
