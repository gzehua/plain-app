package com.ismartcoding.plain.features.dlna

import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.rust.bridge.RustDispatchers
import com.ismartcoding.plain.rust.bridge.RustDlnaBridge
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject

internal object RustDlnaReceiver {
    suspend fun call(command: ReceiverCommand): JsonObject = withContext(RustDispatchers.requests) {
        val response = RustDlnaBridge.receiver(JsonHelper.jsonEncode<ReceiverCommand>(command))
        check(!response.startsWith("ERROR:")) { response.removePrefix("ERROR:") }
        JsonHelper.jsonDecode<JsonObject>(response)
    }
}
