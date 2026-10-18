package com.ismartcoding.plain.features.dlna

import com.ismartcoding.plain.lib.JsonHelper

internal object RustDlnaRules {
    suspend fun snapshot(): DlnaRulesSnapshot = JsonHelper.jsonDecodeFromElement(
        RustDlnaReceiver.call(ReceiverCommand.Snapshot).getValue("rules"),
    )
    suspend fun remove(ip: String, allowed: Boolean) = call(ReceiverCommand.RemoveRule(ip, allowed))
    private suspend fun call(command: ReceiverCommand): DlnaRulesSnapshot {
        return JsonHelper.jsonDecodeFromElement<DlnaRulesSnapshot>(RustDlnaReceiver.call(command))
    }
}
