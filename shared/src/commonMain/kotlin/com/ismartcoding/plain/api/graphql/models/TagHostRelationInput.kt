package com.ismartcoding.plain.api.graphql.models

import com.ismartcoding.plain.platform.system.DataType
import kotlinx.serialization.Serializable

@Serializable
data class TagHostRelationInput(
    var tagId: String,
    var key: String,
    var type: DataType,
    var sizeBytes: Long,
    var title: String,
)
