package com.ismartcoding.plain.api.graphql.models

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
