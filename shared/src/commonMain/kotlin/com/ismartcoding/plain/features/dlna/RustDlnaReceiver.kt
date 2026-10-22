package com.ismartcoding.plain.features.dlna

import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.rust.PlainRust
import kotlinx.serialization.json.*

internal object RustDlnaReceiver {
    suspend fun call(command: ReceiverCommand): JsonObject = when (command) {
        ReceiverCommand.Snapshot -> PlainRust.mainQuery("dlnaReceiver").getValue("dlnaReceiver").jsonObject
        else -> PlainRust.mainGraphql(
            "mutation(\$input: DlnaReceiverInput!) { controlDlnaReceiver(input: \$input) }",
            mapOf("input" to command.input()),
        ).getValue("controlDlnaReceiver").jsonObject
    }
}

internal fun ReceiverCommand.input(): JsonObject = JsonHelper.jsonEncodeToElement<ReceiverCommand>(this).jsonObject
