package com.ismartcoding.plain.features.dlna

import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.rust.request.RustContentApi
import kotlinx.serialization.json.*

internal object RustDlnaReceiver {
    suspend fun call(command: ReceiverCommand): JsonObject = when (command) {
        ReceiverCommand.Snapshot -> RustContentApi.query("dlnaReceiver").getValue("dlnaReceiver").jsonObject
        else -> RustContentApi.graphql(
            "mutation(\$input: DlnaReceiverInput!) { controlDlnaReceiver(input: \$input) }",
            mapOf("input" to command.input()),
        ).getValue("controlDlnaReceiver").jsonObject
    }
}

internal fun ReceiverCommand.input(): JsonObject {
    val values = JsonHelper.jsonEncodeToElement<ReceiverCommand>(this).jsonObject.toMutableMap()
    for (name in listOf("action")) {
        values[name]?.jsonPrimitive?.content?.let { value ->
            values[name] = JsonPrimitive(value.replace(Regex("([a-z])([A-Z])"), "$1_$2").uppercase())
        }
    }
    return JsonObject(values)
}
