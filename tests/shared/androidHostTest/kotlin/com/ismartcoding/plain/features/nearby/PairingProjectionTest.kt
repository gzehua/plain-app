package com.ismartcoding.plain.features.nearby

import com.ismartcoding.plain.ui.nearby.NearbyItemStatus
import com.ismartcoding.plain.ui.nearby.NearbyViewModel
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class PairingProjectionTest {
    @AfterTest
    fun clear() {
        NearbyViewModel.pairingStates.clear()
        NearbyViewModel.itemStatus.clear()
    }

    private fun state(generation: String, phase: String) = Json.parseToJsonElement("""{"deviceId":"peer","generation":"$generation","mode":"CHAT","phase":"$phase","error":null}""").jsonObject

    @Test
    fun cachedGenerationsProjectWithoutANativeBridgeAndOldEventsCannotClearNewWork() = runBlocking {
        PairingProjection.applyState(state("first", "STARTING"))
        assertEquals(NearbyItemStatus.STARTING, NearbyViewModel.itemStatus["peer"])
        PairingProjection.applyState(state("first", "CANCELLED"))
        PairingProjection.applyState(state("second", "STARTING"))
        assertEquals(NearbyItemStatus.STARTING, NearbyViewModel.itemStatus["peer"])
        PairingProjection.applyState(state("second", "SUCCEEDED"))
        assertEquals(NearbyItemStatus.PAIRED, NearbyViewModel.itemStatus["peer"])
    }

    @Test
    fun unknownPhasesRemainPendingAndOlderSnapshotsCannotRegressKnownProgress() {
        PairingProjection.applyState(state("first", "COMMITTING"))
        PairingProjection.applyState(state("first", "STARTING"))
        assertEquals("COMMITTING", NearbyViewModel.pairingStates["first"]?.get("phase")?.toString()?.trim('"'))
        PairingProjection.applyState(state("other", "FUTURE_PHASE"))
        assertEquals(NearbyItemStatus.PAIRING, NearbyViewModel.itemStatus["peer"])
    }
    @Test
    fun anOlderSnapshotCannotRemoveAnAttemptStartedAfterTheRead() {
        PairingProjection.applyState(state("first", "STARTING"))
        val before = NearbyViewModel.pairingStates.toMap()
        PairingProjection.applyState(state("first", "CANCELLED"))
        PairingProjection.applyState(state("second", "PASSWORD_REQUIRED"))
        PairingProjection.applySnapshot(before, listOf(state("first", "STARTING")))
        assertEquals("PASSWORD_REQUIRED", NearbyViewModel.pairingStates["second"]?.get("phase")?.toString()?.trim('"'))
        assertEquals(NearbyItemStatus.PAIRING, NearbyViewModel.itemStatus["peer"])
        PairingProjection.applySnapshot(NearbyViewModel.pairingStates.toMap(), emptyList())
        assertEquals(null, NearbyViewModel.itemStatus["peer"])
    }

}
