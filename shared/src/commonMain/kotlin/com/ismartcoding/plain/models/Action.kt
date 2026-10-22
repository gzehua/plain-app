package com.ismartcoding.plain.models

import kotlinx.serialization.Serializable

@Serializable
enum class Action {
    TRASH,
    RESTORE,
    DELETE,
    MOVE,
}
