package com.ismartcoding.plain.discover

import com.ismartcoding.plain.rust.PlainRust
import com.ismartcoding.plain.data.DPairingTicket
import com.ismartcoding.plain.lib.JsonHelper

object RustPairingStore {
    suspend fun tickets(): List<DPairingTicket> = JsonHelper.jsonDecodeFromElement(
        PlainRust.mainQuery("pairingTickets { deviceId deviceName deviceIp devicePort generation delayMs }").getValue("pairingTickets")
    )
}
