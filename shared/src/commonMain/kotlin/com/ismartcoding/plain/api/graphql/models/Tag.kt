package com.ismartcoding.plain.api.graphql.models

import kotlinx.serialization.Serializable

@Serializable
data class Tag(
    override var id: String,
    var name: String,
    var count: Int,
    var type: Int = 0,
) : com.ismartcoding.plain.db.IData
