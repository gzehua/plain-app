package com.ismartcoding.plain.features.chat.models

import com.ismartcoding.plain.lib.JsonHelper
import kotlinx.serialization.Serializable

@Serializable(with = MessageContentSerializer::class)
class MessageContent(val type: MessageType, var value: Any? = null)

fun MessageContent.toJSONString(): String = JsonHelper.jsonEncode(this)
