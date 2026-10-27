package com.ismartcoding.plain.features.nearby

import kotlinx.serialization.Serializable

@Serializable(with = PairingPhaseSerializer::class)
enum class PairingPhase {
    UNKNOWN,
    STARTING,
    PASSWORD_REQUIRED,
    AUTHENTICATING,
    PAIRING,
    WAITING_CONFIRMATION,
    COMMITTING,
    SUCCEEDED,
    FAILED,
    CANCELLED,
}
