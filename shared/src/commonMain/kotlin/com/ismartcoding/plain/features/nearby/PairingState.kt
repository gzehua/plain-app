package com.ismartcoding.plain.features.nearby

import com.ismartcoding.plain.lib.JsonHelper
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
internal data class PairingState(
    val generation: String,
    val deviceId: String? = null,
    val phase: String? = null,
    val error: String? = null,
    val verificationCode: String? = null,
)

internal fun JsonObject.pairingState(): PairingState = JsonHelper.jsonDecodeFromElement(this)
