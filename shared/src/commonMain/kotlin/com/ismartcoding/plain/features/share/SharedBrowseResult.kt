package com.ismartcoding.plain.features.share

import com.ismartcoding.plain.chat.models.MessageShare
import kotlinx.serialization.Serializable

@Serializable
data class SharedBrowseResult(val info: SharedInfoDto, val link: SharedLink, val card: MessageShare)
