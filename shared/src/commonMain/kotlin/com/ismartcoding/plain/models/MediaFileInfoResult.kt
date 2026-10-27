package com.ismartcoding.plain.models

import kotlinx.serialization.Serializable

@Serializable
data class MediaFileInfoResult<T>(val data: T?)
