package com.ismartcoding.plain.features.nearby

import com.ismartcoding.plain.api.graphql.models.PairingTicket
import com.ismartcoding.plain.rust.PlainRust
import com.ismartcoding.plain.lib.JsonHelper

object RustPairingStore {
    suspend fun tickets(): List<PairingTicket> = JsonHelper.jsonDecodeFromElement(
        PlainRust.mainQuery("pairingTickets { deviceId deviceName deviceIp devicePort generation delayMs }").getValue("pairingTickets")
    )
}
