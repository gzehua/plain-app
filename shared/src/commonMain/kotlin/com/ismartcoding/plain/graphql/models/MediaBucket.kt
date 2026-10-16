package com.ismartcoding.plain.graphql.models

import kotlinx.serialization.Serializable

@Serializable
data class MediaBucket(
    override var id: String,
    var name: String,
    var itemCount: Int,
    var topItemPaths: List<String>,
    var size: Long = 0,
) : com.ismartcoding.plain.db.IData
