package com.ismartcoding.plain.rust.event

import com.ismartcoding.plain.app.events.HostEvent
import com.ismartcoding.plain.app.events.HostEventData
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.lib.logcat.LogCat
import com.ismartcoding.plain.rust.native.RustNativeApi
import com.ismartcoding.plain.rust.native.RustDispatchers
import com.ismartcoding.plain.rust.native.readRustStream
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

object RustEventHandler {
    private val incoming = MutableSharedFlow<RustEvent>(extraBufferCapacity = 64)
    val events: SharedFlow<RustEvent> = incoming.asSharedFlow()
    private val capabilities = MutableStateFlow<HostEventCapabilities?>(null)

    internal fun start(scope: CoroutineScope) {
        capabilities.value = JsonHelper.jsonDecode(RustNativeApi.next("capabilities"))
        scope.launch(start = CoroutineStart.UNDISPATCHED) {
            events.collect { event ->
                try {
                    RustEventProjection.apply(event)
                    if (event.reconcile) RustEventProjection.reconcile()
                } catch (cancelled: CancellationException) { throw cancelled }
                catch (error: Exception) { LogCat.e("Rust event projection", error) }
            }
        }
        scope.launch {
            try { RustEventProjection.reconcile() }
            catch (cancelled: CancellationException) { throw cancelled }
            catch (error: Exception) { LogCat.e("Rust initial reconciliation", error) }
            readRustStream(
                next = { withContext(RustDispatchers.events) { RustNativeApi.next("events") } },
                consume = { incoming.emit(JsonHelper.jsonDecode<RustEvent>(it)) },
                closed = {},
            )
        }
    }

    suspend fun publish(event: HostEvent): Int {
        val capabilities = withTimeoutOrNull(5_000) { capabilities.filterNotNull().first() } ?: error("Rust host event channel unavailable")
        require(capabilities.accepts(event.type, event.data)) { "${event.type} is not a host-produced event" }
        val error = withContext(RustDispatchers.requests) {
            when (val data = event.data) {
                is HostEventData.Text -> RustNativeApi.send("text", JsonHelper.jsonEncode(HostEventPacket(event.type.name, data.value)), byteArrayOf())
                is HostEventData.Binary -> RustNativeApi.send("binary", event.type.name, data.value)
            }
        }
        require(error.isEmpty()) { error }
        return com.ismartcoding.plain.features.sessions.onlineClientIds.value.size
    }
}
