package com.ismartcoding.plain.rust.bridge

import com.ismartcoding.plain.lib.JsonHelper
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive

internal object RustBridgeReply {
    fun value(reply: String): String {
        val json = JsonHelper.jsonDecode<JsonObject>(reply)
        json["error"]?.let { error(it.jsonPrimitive.content) }
        return json.getValue("value").jsonPrimitive.content
    }
}
