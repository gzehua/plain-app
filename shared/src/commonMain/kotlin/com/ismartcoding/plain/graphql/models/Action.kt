package com.ismartcoding.plain.graphql.models

import kotlinx.serialization.Serializable

@Serializable
enum class Action {
    TRASH,
    RESTORE,
    DELETE,
    MOVE,
}
