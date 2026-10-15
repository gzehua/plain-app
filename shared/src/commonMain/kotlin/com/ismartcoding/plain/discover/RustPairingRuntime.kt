package com.ismartcoding.plain.discover

import com.ismartcoding.plain.rust.PlainRust
import com.ismartcoding.plain.rust.content.gql
import com.ismartcoding.plain.data.*
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.events.EventType
import com.ismartcoding.plain.events.HostEvent
import kotlinx.serialization.json.*

object RustPairingRuntime {
    suspend fun states(): JsonArray = PlainRust.mainQuery("pairingStates { deviceId generation phase }").getValue("pairingStates").jsonArray

    suspend fun start(device: DNearbyDevice): JsonElement {
        val input = buildJsonObject {
            put("id", device.id); put("name", device.name); put("port", device.port)
            put("ips", JsonArray(device.ips.map(::JsonPrimitive)))
            put("deviceType", device.deviceType.name); put("version", device.version); put("platform", device.platform)
            put("lastSeen", device.lastSeen.toString())
            put("discoveryMethods", JsonArray(device.discoveryMethods.map { JsonPrimitive(it.name) }))
        }
        val result = PlainRust.mainGraphql("mutation(\$input: PairingDeviceInput!) { pairDevice(input: \$input) }", mapOf("input" to input), longRunning = true).getValue("pairDevice")
        PairingProjection.reconcile()
        return result
    }
    suspend fun cancel(id: String): JsonElement = PlainRust.mainMutate("cancelPairing(deviceId: ${gql(id)})", longRunning = true).getValue("cancelPairing")
    suspend fun currentRequest(id: String, signature: String): Boolean =
        PlainRust.mainQuery("isPairingRequestCurrent(id: ${gql(id)}, signature: ${gql(signature)})").getValue("isPairingRequestCurrent").jsonPrimitive.boolean

    suspend fun receiveRequest(request: DPairingRequest, address: String) {
        PlainRust.publish(HostEvent(EventType.PAIRING_REQUEST_REPORTED, buildJsonObject {
            put("request", JsonHelper.jsonEncodeToElement(request)); put("address", address)
        }.toString()))
    }
    suspend fun receiveCancel(cancel: DPairingCancel) {
        PlainRust.publish(HostEvent(EventType.PAIRING_CANCEL_REPORTED, JsonHelper.jsonEncode(cancel)))
    }
    suspend fun complete(response: DPairingResponse) {
        PlainRust.publish(HostEvent(EventType.PAIRING_RESPONSE_REPORTED, JsonHelper.jsonEncode(response)))
    }
    suspend fun respond(request: DPairingRequest, accepted: Boolean): JsonElement =
        PlainRust.mainGraphql("mutation(\$input: PairingRequestInput!, \$accepted: Boolean!) { respondToPairing(input: \$input, accepted: \$accepted) }", mapOf("input" to JsonHelper.jsonEncodeToElement(request), "accepted" to JsonPrimitive(accepted)), longRunning = true).getValue("respondToPairing")
}
