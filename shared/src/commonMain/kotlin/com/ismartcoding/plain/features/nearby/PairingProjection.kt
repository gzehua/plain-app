package com.ismartcoding.plain.features.nearby

import com.ismartcoding.plain.api.graphql.models.PairingRequestInput
import com.ismartcoding.plain.ui.nearby.NearbyItemStatus
import com.ismartcoding.plain.ui.nearby.NearbyViewModel
import kotlinx.serialization.json.*

// Rust broadcasts pairing events to both mobile and web clients; projections must not republish them.
object PairingProjection {
    suspend fun reconcile() {
        val before = NearbyViewModel.pairingStates.toMap()
        applySnapshot(before, RustPairingRuntime.states().map { it.jsonObject })
    }

    internal fun applySnapshot(before: Map<String, JsonObject>, states: List<JsonObject>) {
        val generations = states.map { it.getValue("generation").jsonPrimitive.content }.toSet()
        val removed = mutableSetOf<String>()
        before.forEach { (generation, state) ->
            if (generation !in generations && NearbyViewModel.pairingStates[generation] == state) {
                NearbyViewModel.pairingStates.remove(generation)
                state["deviceId"]?.jsonPrimitive?.contentOrNull?.let(removed::add)
            }
        }
        states.forEach(::applyState)
        removed.forEach { id ->
            if (NearbyViewModel.pairingStates.values.none { it["deviceId"]?.jsonPrimitive?.contentOrNull == id } && NearbyViewModel.itemStatus[id] in setOf(NearbyItemStatus.STARTING, NearbyItemStatus.PAIRING)) NearbyViewModel.itemStatus.remove(id)
        }
    }

    fun applyState(value: JsonObject) {
        val id = value["deviceId"]?.jsonPrimitive?.contentOrNull ?: return
        val generation = value.getValue("generation").jsonPrimitive.content
        val phases = listOf("STARTING", "PASSWORD_REQUIRED", "AUTHENTICATING", "PAIRING", "WAITING_CONFIRMATION", "COMMITTING", "SUCCEEDED", "FAILED", "CANCELLED")
        val previous = NearbyViewModel.pairingStates[generation]?.get("phase")?.jsonPrimitive?.content
        val next = value["phase"]?.jsonPrimitive?.content
        if (previous != null && phases.indexOf(next) >= 0 && phases.indexOf(previous) > phases.indexOf(next)) return
        NearbyViewModel.pairingStates[generation] = value
        val active = NearbyViewModel.pairingStates.values.firstOrNull {
            it["deviceId"]?.jsonPrimitive?.contentOrNull == id && it["phase"]?.jsonPrimitive?.content !in setOf("SUCCEEDED", "FAILED", "CANCELLED")
        }
        NearbyViewModel.itemStatus[id] = when ((active ?: value)["phase"]?.jsonPrimitive?.content) {
            "SUCCEEDED" -> NearbyItemStatus.PAIRED
            "FAILED", "CANCELLED" -> NearbyItemStatus.UNPAIRED
            "STARTING" -> NearbyItemStatus.STARTING
            else -> NearbyItemStatus.PAIRING
        }
    }

    suspend fun started(payload: String) {
        val value = Json.parseToJsonElement(payload).jsonObject
        val id = value.getValue("deviceId").jsonPrimitive.content
        val generation = value.getValue("generation").jsonPrimitive.content
        if (!NearbyViewModel.pairingStates.containsKey(generation)) reconcile()
        val state = NearbyViewModel.pairingStates[generation] ?: return
        if (state["deviceId"]?.jsonPrimitive?.contentOrNull != id || state["phase"]?.jsonPrimitive?.content in terminalPhases) return
        NearbyViewModel.itemStatus[id] = NearbyItemStatus.PAIRING
    }

    suspend fun request(payload: String) {
        val request = com.ismartcoding.plain.lib.JsonHelper.jsonDecode<com.ismartcoding.plain.api.graphql.models.PairingRequestInput>(payload)
        if (!RustPairingRuntime.currentRequest(request.fromId, request.signature)) return
        com.ismartcoding.plain.lib.sendEvent(com.ismartcoding.plain.app.events.HPairingRequestReceivedEvent(request))
    }

    private val terminalPhases = setOf("SUCCEEDED", "FAILED", "CANCELLED")
    private fun hasActive(id: String): Boolean = NearbyViewModel.pairingStates.values.any {
        it["deviceId"]?.jsonPrimitive?.contentOrNull == id && it["phase"]?.jsonPrimitive?.content !in terminalPhases
    }
    private suspend fun reconcileUnknown(id: String) {
        if (NearbyViewModel.pairingStates.values.none { it["deviceId"]?.jsonPrimitive?.contentOrNull == id }) reconcile()
    }
    suspend fun canceled(payload: String) {
        val value = Json.parseToJsonElement(payload).jsonObject
        val id = value.getValue("deviceId").jsonPrimitive.content
        if (hasActive(id)) return
        reconcileUnknown(id)
        if (hasActive(id)) return
        NearbyViewModel.itemStatus.remove(id)
        com.ismartcoding.plain.lib.sendEvent(com.ismartcoding.plain.app.events.HPairingCanceledEvent(id))
    }

    suspend fun success(payload: String) {
        val value = Json.parseToJsonElement(payload).jsonObject
        val peer = com.ismartcoding.plain.features.chat.peer.RustPeerStore.getById(value.getValue("deviceId").jsonPrimitive.content) ?: return
        if (!peer.isPaired() || peer.key != value.getValue("key").jsonPrimitive.content) return
        com.ismartcoding.plain.features.chat.peer.PeerManager.load()
        NearbyViewModel.handlePairingSuccess(peer.id)
        com.ismartcoding.plain.lib.sendEvent(com.ismartcoding.plain.app.events.HPairingSuccessEvent(
            peer.id, peer.name, value.getValue("ip").jsonPrimitive.content, peer.key,
        ))
    }

    suspend fun timeout(payload: String) {
        val value = Json.parseToJsonElement(payload).jsonObject
        val id = value.getValue("deviceId").jsonPrimitive.content
        if (hasActive(id)) return
        reconcileUnknown(id)
        if (hasActive(id)) return
        NearbyViewModel.itemStatus.remove(id)
    }
}
