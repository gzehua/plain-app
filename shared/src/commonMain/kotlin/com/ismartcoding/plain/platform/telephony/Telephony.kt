package com.ismartcoding.plain.platform.telephony

import com.ismartcoding.plain.api.graphql.models.Sim

/**
 * Returns the list of active SIM subscriptions on the device.
 * Returns an empty list on platforms without telephony support or when the
 * READ_PHONE_STATE permission is not granted.
 */
expect fun getSims(): List<Sim>
