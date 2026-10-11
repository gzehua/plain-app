package com.ismartcoding.plain.chat.models

import kotlinx.serialization.Serializable

@Serializable
class MessageText(val text: String, val linkPreviews: List<LinkPreview> = emptyList())
