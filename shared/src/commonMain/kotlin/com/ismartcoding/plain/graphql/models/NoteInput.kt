package com.ismartcoding.plain.graphql.models

import kotlinx.serialization.Serializable

@Serializable
data class NoteInput(
    var title: String,
    var content: String,
)
