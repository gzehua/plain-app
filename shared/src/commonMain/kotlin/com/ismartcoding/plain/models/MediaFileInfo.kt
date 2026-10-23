package com.ismartcoding.plain.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator
import kotlinx.serialization.ExperimentalSerializationApi

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("__typename")
sealed interface MediaFileInfo
