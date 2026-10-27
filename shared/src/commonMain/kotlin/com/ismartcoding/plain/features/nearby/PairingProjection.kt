package com.ismartcoding.plain.features.nearby

import com.ismartcoding.plain.api.graphql.models.PairingSuccessNotice
import com.ismartcoding.plain.lib.JsonHelper
import kotlinx.serialization.Serializable

import com.ismartcoding.plain.models.PairingRequestInput
import com.ismartcoding.plain.ui.nearby.NearbyItemStatus
import com.ismartcoding.plain.ui.nearby.NearbyViewModel
import kotlinx.serialization.json.*
import com.ismartcoding.plain.ui.app.MainViewModel
import com.ismartcoding.plain.features.chat.peer.RustPeerStore
import com.ismartcoding.plain.features.chat.peer.PeerStatusClient

object PairingProjection {
    suspend fun reconcile() {
        val before = NearbyViewModel.pairingStates.toMap()
        applySnapshot(before, RustPairingRuntime.states().map { it.jsonObject })
        MainViewModel.pendingPairingRequest.value?.let { request ->
            if (!RustPairingRuntime.currentRequest(request.fromId, request.signature) && MainViewModel.pendingPairingRequest.value == request) MainViewModel.pendingPairingRequest.value = null
        }
    }

    internal fun applySnapshot(before: Map<String, JsonObject>, states: List<JsonObject>) {
        val generations = states.map { it.pairingState().generation }.toSet()
        val removed = mutableSetOf<String>()
        before.forEach { (generation, state) ->
            if (generation !in generations && NearbyViewModel.pairingStates[generation] == state) {
                NearbyViewModel.pairingStates.remove(generation)
                state.pairingState().deviceId?.let(removed::add)
            }
        }
        states.forEach(::applyState)
        removed.forEach { id ->
            if (NearbyViewModel.pairingStates.values.none { it.pairingState().deviceId == id } && NearbyViewModel.itemStatus[id] in setOf(NearbyItemStatus.STARTING, NearbyItemStatus.PAIRING)) NearbyViewModel.itemStatus.remove(id)
        }
    }

    fun applyState(value: JsonObject) {
        val state = value.pairingState()
        val id = state.deviceId ?: return
        val generation = state.generation
        val phases = listOf("STARTING", "PASSWORD_REQUIRED", "AUTHENTICATING", "PAIRING", "WAITING_CONFIRMATION", "COMMITTING", "SUCCEEDED", "FAILED", "CANCELLED")
        val previous = NearbyViewModel.pairingStates[generation]?.pairingState()?.phase
        val next = state.phase
        if (previous != null && phases.indexOf(next) >= 0 && phases.indexOf(previous) > phases.indexOf(next)) return
        NearbyViewModel.pairingStates[generation] = value
        val active = NearbyViewModel.pairingStates.values.firstOrNull {
            it.pairingState().let { state -> state.deviceId == id && state.phase !in terminalPhases }
        }
        NearbyViewModel.itemStatus[id] = when ((active ?: value).pairingState().phase) {
            "SUCCEEDED" -> NearbyItemStatus.PAIRED
            "FAILED", "CANCELLED" -> NearbyItemStatus.UNPAIRED
            "STARTING" -> NearbyItemStatus.STARTING
            else -> NearbyItemStatus.PAIRING
        }
    }

    suspend fun request(request: PairingRequestInput) {
        if (!RustPairingRuntime.currentRequest(request.fromId, request.signature)) return
        MainViewModel.pendingPairingRequest.value = request
    }

    private val terminalPhases = setOf("SUCCEEDED", "FAILED", "CANCELLED")
    internal suspend fun success(value: PairingSuccessNotice) {
        val peer = RustPeerStore.getById(value.deviceId) ?: return
        if (!peer.isPaired() || peer.key != value.key) return
        NearbyViewModel.handlePairingSuccess(peer.id)
        PeerStatusClient.reconnectNow("post_pairing")
    }
}
