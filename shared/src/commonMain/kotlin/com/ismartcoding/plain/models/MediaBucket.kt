package com.ismartcoding.plain.models

import com.ismartcoding.plain.IData
import kotlinx.serialization.Serializable

@Serializable
data class MediaBucket(
    override var id: String,
    var name: String,
    var itemCount: Int,
    var topItemPaths: List<String>,
    var size: Long = 0,
) : IData
