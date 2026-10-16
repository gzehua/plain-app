package com.ismartcoding.plain.features.chat.models

import kotlinx.serialization.Serializable

@Serializable
class MessageText(val text: String, val linkPreviews: List<LinkPreview> = emptyList())
