package com.ismartcoding.plain.api.graphql.models

import kotlinx.serialization.Serializable

@Serializable
enum class Action {
    TRASH,
    RESTORE,
    DELETE,
    MOVE,
}
