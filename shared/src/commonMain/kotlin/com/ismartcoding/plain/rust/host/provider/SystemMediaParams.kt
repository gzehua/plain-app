package com.ismartcoding.plain.rust.host.provider

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive

internal fun mediaDataType(params: JsonObject) =
    com.ismartcoding.plain.platform.system.DataType.valueOf(params.getValue("dataType").jsonPrimitive.content)
