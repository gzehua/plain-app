package com.ismartcoding.plain.graphql.models

import kotlinx.serialization.Serializable

@kotlinx.serialization.SerialName("ImageFileInfo")
@Serializable
data class ImageFileInfo(
    var width: Int,
    var height: Int,
    var location: Location?,
) : MediaFileInfo
