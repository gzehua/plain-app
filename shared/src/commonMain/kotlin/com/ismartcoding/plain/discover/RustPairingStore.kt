package com.ismartcoding.plain.discover

import com.ismartcoding.plain.graphql.models.PairingTicket
import com.ismartcoding.plain.rust.PlainRust
import com.ismartcoding.plain.lib.JsonHelper

object RustPairingStore {
    suspend fun tickets(): List<PairingTicket> = JsonHelper.jsonDecodeFromElement(
        PlainRust.mainQuery("pairingTickets { deviceId deviceName deviceIp devicePort generation delayMs }").getValue("pairingTickets")
    )
}
