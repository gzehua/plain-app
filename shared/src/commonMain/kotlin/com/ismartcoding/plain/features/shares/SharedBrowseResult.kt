package com.ismartcoding.plain.features.shares

import com.ismartcoding.plain.features.chat.models.MessageShare
import kotlinx.serialization.Serializable

@Serializable
data class SharedBrowseResult(val info: SharedInfoDto, val link: SharedLink, val card: MessageShare)
