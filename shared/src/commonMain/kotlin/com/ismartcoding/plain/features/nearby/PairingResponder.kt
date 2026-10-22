package com.ismartcoding.plain.features.nearby

import com.ismartcoding.plain.models.PairingRequestInput
import com.ismartcoding.plain.lib.withIO

object PairingResponder {
    suspend fun respond(request: PairingRequestInput, accepted: Boolean) = withIO { RustPairingRuntime.respond(request, accepted) }
}
