package com.ismartcoding.plain.platform.network.ble

import com.ismartcoding.plain.rust.request.RustRuntime
import com.ismartcoding.plain.rust.request.ApiSession
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.app.*
import com.ismartcoding.plain.ui.app.*
import com.ismartcoding.plain.features.chat.*
import com.ismartcoding.plain.ui.chat.*
import com.ismartcoding.plain.features.feeds.*
import com.ismartcoding.plain.ui.feeds.*
import com.ismartcoding.plain.features.images.search.*
import com.ismartcoding.plain.features.sessions.*
import com.ismartcoding.plain.ui.sessions.*
import com.ismartcoding.plain.features.shares.*
import com.ismartcoding.plain.ui.shares.*
import com.ismartcoding.plain.platform.*
import com.ismartcoding.plain.platform.contacts.*
import com.ismartcoding.plain.platform.files.*
import com.ismartcoding.plain.ui.platform.files.*
import com.ismartcoding.plain.platform.media.*
import com.ismartcoding.plain.ui.platform.media.*
import com.ismartcoding.plain.platform.network.*
import com.ismartcoding.plain.platform.permissions.*
import com.ismartcoding.plain.app.*
import com.ismartcoding.plain.ui.app.*
import com.ismartcoding.plain.ui.navigation.*
import com.ismartcoding.plain.features.sessions.*
import com.ismartcoding.plain.ui.sessions.*
import com.ismartcoding.plain.features.settings.*
import com.ismartcoding.plain.ui.settings.*
import com.ismartcoding.plain.platform.files.*
import com.ismartcoding.plain.ui.platform.files.*
import com.ismartcoding.plain.platform.media.*
import com.ismartcoding.plain.ui.platform.media.*
import com.ismartcoding.plain.platform.notifications.*
import com.ismartcoding.plain.platform.system.*
import com.ismartcoding.plain.ui.platform.system.*
import com.ismartcoding.plain.ui.state.*
import com.ismartcoding.plain.platform.telephony.*
import com.ismartcoding.plain.rust.http.*
import kotlinx.coroutines.*
import kotlinx.coroutines.selects.select
import kotlinx.serialization.Serializable

internal object BleDataChannel {
    private val client by lazy { createPlainHttpClient(PlainHttpClientSpec.HttpHost) }

    /**
     * Handles one *incoming* peer BLE request: Rust dispatches the assembled
     * request over `/chat/ble-incoming` and returns the encoded response. The
     * outgoing direction no longer round-trips through here — Rust asks the App
     * for the whole exchange in a single host call (see `BleTransport`).
     */
    suspend fun incoming(remoteHost: String, characteristicUuid: String, message: ByteArray): ByteArray = withTimeout(120_000) {
        val target = session()
        var response: ByteArray? = null
        client.webSocket("${target.baseUrl.replace("http://", "ws://")}/chat/ble-incoming", target.headers()) { socket ->
            socket.sendText(JsonHelper.jsonEncode(ConnectionFacts(remoteHost, characteristicUuid)))
            socket.sendBinary(message)
            response = withTimeout(120_000) { checkNotNull(socket.incoming.receive().binary) { "Expected BLE response bytes" } }
        }
        checkNotNull(response)
    }
    private suspend fun session(): ApiSession { return RustRuntime.transportSession() }
    @Serializable private data class ConnectionFacts(val remoteHost: String, val characteristicUuid: String)
}
