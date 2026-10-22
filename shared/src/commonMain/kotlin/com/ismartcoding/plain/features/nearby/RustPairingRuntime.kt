package com.ismartcoding.plain.features.nearby

import com.ismartcoding.plain.rust.event.RustEventHandler
import com.ismartcoding.plain.rust.graphql.RustGraphqlApi

import com.ismartcoding.plain.models.NearbyDevice
import com.ismartcoding.plain.models.PairingRequestInput
import com.ismartcoding.plain.models.PairingDeviceInput
import com.ismartcoding.plain.rust.graphql.gql
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.app.events.EventType
import com.ismartcoding.plain.app.events.HostEvent
import kotlinx.serialization.json.*

object RustPairingRuntime {
    const val STATE_FIELDS = "deviceId generation mode phase error verificationCode"
    suspend fun states(): JsonArray = RustGraphqlApi.query("pairingStates { $STATE_FIELDS }").getValue("pairingStates").jsonArray

    suspend fun start(device: NearbyDevice, mode: String = "CHAT"): JsonObject {
        val ip = if ("QR" in device.discoveryMethods) device.bestIp.takeIf { it.isNotBlank() } ?: device.ips.firstOrNull() else null
        val host = ip?.let { "${if (':' in it) "[$it]" else it}:${device.port}" }
        return execute(PairingDeviceInput(id = device.id, host = host, mode = mode)).also { PairingProjection.reconcile() }
    }
    suspend fun submit(generation: String, password: String): JsonObject = execute(PairingDeviceInput(generation = generation, password = password))
    private suspend fun execute(input: PairingDeviceInput): JsonObject = RustGraphqlApi.graphql(
        "mutation(\$input: PairingDeviceInput!) { pairDevice(input: \$input) { $STATE_FIELDS } }",
        mapOf("input" to JsonHelper.jsonEncodeToElement(input)), longRunning = true,
    ).getValue("pairDevice").jsonObject
    suspend fun cancelGeneration(generation: String): JsonElement = RustGraphqlApi.mutate("cancelPairing(generation: ${gql(generation)})", longRunning = true).getValue("cancelPairing")
    suspend fun cancel(id: String): JsonElement {
        val attempt = JsonHelper.jsonDecodeFromElement<List<PairingState>>(states()).lastOrNull { it.deviceId == id && it.phase !in setOf("SUCCEEDED", "FAILED", "CANCELLED") } ?: return JsonPrimitive(false)
        return cancelGeneration(attempt.generation)
    }
    suspend fun currentRequest(id: String, signature: String): Boolean =
        RustGraphqlApi.query("isPairingRequestCurrent(id: ${gql(id)}, signature: ${gql(signature)})").getValue("isPairingRequestCurrent").jsonPrimitive.boolean

    suspend fun complete(payload: String) {
        RustEventHandler.publish(HostEvent(EventType.PAIRING_RESPONSE_REPORTED, payload))
    }
    suspend fun respond(request: PairingRequestInput, accepted: Boolean): JsonElement =
        RustGraphqlApi.graphql("mutation(\$input: PairingRequestInput!, \$accepted: Boolean!) { respondToPairing(input: \$input, accepted: \$accepted) }", mapOf("input" to JsonHelper.jsonEncodeToElement(request), "accepted" to JsonPrimitive(accepted)), longRunning = true).getValue("respondToPairing")
}
