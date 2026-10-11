package com.ismartcoding.plain.graphql.models

import com.ismartcoding.plain.enums.DataType
import kotlinx.serialization.Serializable

@Serializable
data class TagHostRelationInput(
    var tagId: String,
    var key: String,
    var type: DataType,
    var sizeBytes: Long,
    var title: String,
)
