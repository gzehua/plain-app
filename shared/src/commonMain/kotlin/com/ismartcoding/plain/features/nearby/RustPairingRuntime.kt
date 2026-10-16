package com.ismartcoding.plain.features.nearby

import com.ismartcoding.plain.api.graphql.models.NearbyDevice
import com.ismartcoding.plain.api.graphql.models.PairingRequestInput
import com.ismartcoding.plain.api.graphql.models.PairingDeviceInput
import com.ismartcoding.plain.platform.system.DeviceType
import com.ismartcoding.plain.features.nearby.DiscoveryMethod
import kotlin.time.Instant
import com.ismartcoding.plain.rust.PlainRust
import com.ismartcoding.plain.rust.request.gql
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.app.events.EventType
import com.ismartcoding.plain.app.events.HostEvent
import kotlinx.serialization.json.*

object RustPairingRuntime {
    suspend fun states(): JsonArray = PlainRust.mainQuery("pairingStates { deviceId generation phase }").getValue("pairingStates").jsonArray

    suspend fun start(device: NearbyDevice): JsonElement {
        val input = JsonHelper.jsonEncodeToElement(PairingDeviceInput(
            id = device.id,
            name = device.name,
            ips = device.ips,
            port = device.port,
            deviceType = DeviceType.valueOf(device.deviceType),
            version = device.version,
            platform = device.platform,
            lastSeen = Instant.parse(device.lastSeen),
            discoveryMethods = device.discoveryMethods.map(DiscoveryMethod::valueOf),
        ))
        val result = PlainRust.mainGraphql("mutation(\$input: PairingDeviceInput!) { pairDevice(input: \$input) }", mapOf("input" to input), longRunning = true).getValue("pairDevice")
        PairingProjection.reconcile()
        return result
    }
    suspend fun cancel(id: String): JsonElement = PlainRust.mainMutate("cancelPairing(deviceId: ${gql(id)})", longRunning = true).getValue("cancelPairing")
    suspend fun currentRequest(id: String, signature: String): Boolean =
        PlainRust.mainQuery("isPairingRequestCurrent(id: ${gql(id)}, signature: ${gql(signature)})").getValue("isPairingRequestCurrent").jsonPrimitive.boolean

    suspend fun receiveRequest(payload: String, address: String) {
        PlainRust.publish(HostEvent(EventType.PAIRING_REQUEST_REPORTED, pairingRequestReport(payload, address)))
    }
    suspend fun receiveCancel(payload: String) {
        PlainRust.publish(HostEvent(EventType.PAIRING_CANCEL_REPORTED, payload))
    }
    suspend fun complete(payload: String) {
        PlainRust.publish(HostEvent(EventType.PAIRING_RESPONSE_REPORTED, payload))
    }
    suspend fun respond(request: PairingRequestInput, accepted: Boolean): JsonElement =
        PlainRust.mainGraphql("mutation(\$input: PairingRequestInput!, \$accepted: Boolean!) { respondToPairing(input: \$input, accepted: \$accepted) }", mapOf("input" to JsonHelper.jsonEncodeToElement(request), "accepted" to JsonPrimitive(accepted)), longRunning = true).getValue("respondToPairing")
}

@kotlinx.serialization.Serializable
private data class PairingRequestReport(val request: JsonElement, val address: String)

internal fun pairingRequestReport(payload: String, address: String): String =
    JsonHelper.jsonEncode(PairingRequestReport(Json.parseToJsonElement(payload), address))
