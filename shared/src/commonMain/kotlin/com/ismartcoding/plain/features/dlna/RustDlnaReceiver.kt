package com.ismartcoding.plain.features.dlna

import com.ismartcoding.plain.rust.native.RustNativeApi
import kotlinx.serialization.json.JsonObject

internal object RustDlnaReceiver {
    suspend fun call(command: ReceiverCommand): JsonObject = when (command) {
        ReceiverCommand.Snapshot -> RustNativeApi.dlnaReceiver()
        ReceiverCommand.Retry -> RustNativeApi.retryDlnaReceiver()
        is ReceiverCommand.RemoveRule -> RustNativeApi.removeDlnaRule(command.ip, command.allowed)
        is ReceiverCommand.Accept -> RustNativeApi.acceptDlnaCast(command.remember)
        is ReceiverCommand.Reject -> RustNativeApi.rejectDlnaCast(command.remember)
    }
}
