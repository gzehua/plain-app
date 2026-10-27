package com.ismartcoding.plain.features.nearby

import com.ismartcoding.plain.rust.native.RustHostReports
import com.ismartcoding.plain.graphql.Mutation
import com.ismartcoding.plain.graphql.Query
import com.ismartcoding.plain.rust.native.RustNativeApi
import com.ismartcoding.plain.rust.graphql.RustGraphqlApi
import com.ismartcoding.plain.models.NearbyDevice
import com.ismartcoding.plain.models.PairingRequestInput
import com.ismartcoding.plain.models.PairingDeviceInput
import com.ismartcoding.plain.lib.JsonHelper
import kotlinx.serialization.json.*

object RustPairingRuntime {
    suspend fun states(): JsonArray = RustGraphqlApi.graphql(Query.pairingStates()).getValue("pairingStates").jsonArray

    suspend fun start(device: NearbyDevice, mode: String = "CHAT"): JsonObject {
        val ip = if ("QR" in device.discoveryMethods) device.bestIp.takeIf { it.isNotBlank() } ?: device.ips.firstOrNull() else null
        val host = ip?.let { "${if (':' in it) "[$it]" else it}:${device.port}" }
        return execute(PairingDeviceInput(id = device.id, host = host, mode = mode)).also { PairingProjection.reconcile() }
    }
    suspend fun submit(generation: String, password: String): JsonObject = execute(PairingDeviceInput(generation = generation, password = password))
    private suspend fun execute(input: PairingDeviceInput): JsonObject = RustGraphqlApi.graphql(
        Mutation.pairDevice(input),
    ).getValue("pairDevice").jsonObject
    suspend fun cancelGeneration(generation: String): JsonElement = RustGraphqlApi.graphql(Mutation.cancelPairing(generation)).getValue("cancelPairing")
    suspend fun cancel(id: String): JsonElement {
        val attempt = JsonHelper.jsonDecodeFromElement<List<PairingState>>(states()).lastOrNull { it.deviceId == id && it.phase !in setOf("SUCCEEDED", "FAILED", "CANCELLED") } ?: return JsonPrimitive(false)
        return cancelGeneration(attempt.generation)
    }
    suspend fun currentRequest(id: String, signature: String): Boolean =
        RustNativeApi.isPairingRequestCurrent(id, signature)

    suspend fun complete(payload: String) {
        RustHostReports.pairingResponse(payload)
    }
    suspend fun respond(request: PairingRequestInput, accepted: Boolean): JsonElement =
        RustGraphqlApi.graphql(Mutation.respondToPairing(request, accepted)).getValue("respondToPairing")
}
