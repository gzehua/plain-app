package com.ismartcoding.plain.discover

import com.ismartcoding.plain.graphql.models.PairingRequestInput
import com.ismartcoding.plain.lib.withIO

object PairingResponder {
    suspend fun respond(request: PairingRequestInput, accepted: Boolean) = withIO { RustPairingRuntime.respond(request, accepted) }
}
