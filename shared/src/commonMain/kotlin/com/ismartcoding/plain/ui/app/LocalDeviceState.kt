package com.ismartcoding.plain.ui.app

import kotlinx.serialization.Serializable

import androidx.compose.runtime.mutableStateOf
import com.ismartcoding.plain.api.graphql.models.NetworkStatus
import com.ismartcoding.plain.api.graphql.models.Peer
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.platform.system.DeviceType
import com.ismartcoding.plain.rust.PlainRust
import kotlinx.coroutines.CancellationException
import com.ismartcoding.plain.lib.logcat.LogCat
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.*

object LocalDeviceState {
    val peer = mutableStateOf<Peer?>(null)
    private val refreshLock = Mutex()

    suspend fun refresh() = refreshLock.withLock {
        try {
            val app = JsonHelper.jsonDecodeFromElement<LocalAppInfo>(PlainRust.mainQuery("app { clientId deviceName deviceType httpsPort network { ipv4Addresses primaryIpv4 vpnActive } }").getValue("app"))
            val network = app.network
            peer.value = Peer(
                key = "",
                publicKey = "",
                token = "",
                status = com.ismartcoding.plain.features.nearby.PeerStatus.UNPAIRED,
                createdAt = kotlin.time.Instant.fromEpochMilliseconds(0),
                updatedAt = kotlin.time.Instant.fromEpochMilliseconds(0),
                id = app.clientId,
                name = app.deviceName,
                ip = network.ipv4Addresses.joinToString(","),
                port = app.httpsPort,
                deviceType = DeviceType.valueOf(app.deviceType),
            )
            AppUiState.ip4s.value = network.ipv4Addresses
            AppUiState.primaryIpv4.value = network.primaryIpv4
            AppUiState.vpnActive.value = network.vpnActive
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            LogCat.e("Local device refresh failed", error)
        }
    }
}

@Serializable
private data class LocalAppInfo(val clientId: String, val deviceName: String, val deviceType: String, val httpsPort: Int, val network: NetworkStatus)
