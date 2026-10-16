package com.ismartcoding.plain.features.nearby

import com.ismartcoding.plain.platform.network.ble.PairingTransport
import com.ismartcoding.plain.platform.network.ble.BleUuids
import kotlinx.serialization.json.*

object PairingNotificationHost {
    fun handle(params: JsonObject): JsonElement = JsonPrimitive(PairingTransport.sendNotification(
        params.getValue("address").jsonPrimitive.content, BleUuids.NEARBY_CHAR_UUID,
        params.getValue("body").jsonPrimitive.content,
    ))
}
