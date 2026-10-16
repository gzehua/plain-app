package com.ismartcoding.plain.rust

import com.ismartcoding.plain.rust.bridge.RustCoreBridge
import com.ismartcoding.plain.rust.bridge.RustDispatchers
import com.ismartcoding.plain.rust.event.HostEventCapabilities
import com.ismartcoding.plain.rust.event.HostEventPacket
import com.ismartcoding.plain.rust.event.RustEvent
import com.ismartcoding.plain.rust.event.RustEventProjection
import com.ismartcoding.plain.rust.host.RustHostApi
import com.ismartcoding.plain.rust.request.ContentApiSession
import com.ismartcoding.plain.rust.request.GraphqlRequest
import com.ismartcoding.plain.events.*
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.lib.logcat.LogCat
import com.ismartcoding.plain.platform.PlainHttpClientSpec
import com.ismartcoding.plain.platform.createPlainHttpClient
import com.ismartcoding.plain.platform.postText
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

object PlainRust {
    private val client by lazy { createPlainHttpClient(PlainHttpClientSpec.Local()) }
    private val transferClient by lazy { createPlainHttpClient(PlainHttpClientSpec.Local(15 * 60)) }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val incoming = MutableSharedFlow<RustEvent>(extraBufferCapacity = 64)
    val events: SharedFlow<RustEvent> = incoming.asSharedFlow()
    internal val hostRequests = Channel<String>(32)
    private var started = false
    private val capabilities = MutableStateFlow<HostEventCapabilities?>(null)

    suspend fun mainQuery(selection: String, session: ContentApiSession? = null): JsonObject =
        data("query { $selection }", session)

    suspend fun mainMutate(selection: String, session: ContentApiSession? = null, longRunning: Boolean = false): JsonObject =
        data("mutation { $selection }", session, longRunning = longRunning)

    suspend fun mainGraphql(document: String, variables: Map<String, JsonElement> = emptyMap(), session: ContentApiSession? = null, longRunning: Boolean = false): JsonObject =
        data(document, session, variables = variables, longRunning = longRunning)

    private suspend fun data(
        document: String,
        session: ContentApiSession?,
        variables: Map<String, JsonElement> = emptyMap(),
        operationName: String? = null,
        longRunning: Boolean = false,
    ): JsonObject = unwrap(if (session == null) {
        native(document, variables, operationName)
    } else {
        remote(document, variables, operationName, session, longRunning)
    })

    private suspend fun native(document: String, variables: Map<String, JsonElement>, operationName: String?): JsonObject =
        withContext(RustDispatchers.requests) {
            JsonHelper.jsonDecode(RustCoreBridge.graphql(JsonHelper.jsonEncode(GraphqlRequest(document, variables, operationName))))
        }

    private suspend fun remote(
        document: String,
        variables: Map<String, JsonElement>,
        operationName: String?,
        session: ContentApiSession,
        longRunning: Boolean,
    ): JsonObject {
        val body = JsonHelper.jsonEncode(GraphqlRequest(document, variables, operationName))
        val response = (if (longRunning) transferClient else client)
            .postText("${session.baseUrl}/graphql", body, "application/json", session.headers())
        response.use {
            check(it.isOk()) { "Rust API returned HTTP ${it.status}" }
            return JsonHelper.jsonDecode(it.bodyAsText())
        }
    }

    private fun unwrap(response: JsonObject): JsonObject {
        val errors = response["errors"] as? JsonArray
        check(errors.isNullOrEmpty()) { errors?.joinToString { error -> error.jsonObject["message"]?.jsonPrimitive?.content ?: "Rust API error" } ?: "Rust API error" }
        return response.getValue("data").jsonObject
    }

    // ── Core lifecycle and native streams ────────────────────────────────

    internal fun initialize(dataDir: String, token: String, config: String): String = RustCoreBridge.start(dataDir, token, config)
    internal fun stopHttp() = RustCoreBridge.stop()

    internal fun start(sessionProvider: () -> ContentApiSession) {
        check(!started) { "PlainRust already started" }
        capabilities.value = JsonHelper.jsonDecode(RustCoreBridge.next("capabilities"))
        started = true
        RustHostApi.start(scope, sessionProvider)
        scope.launch { pump("host") { hostRequests.send(it) } }
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
            pump("events") { incoming.emit(JsonHelper.jsonDecode<RustEvent>(it)) }
        }
    }

    private suspend fun pump(stream: String, consume: suspend (String) -> Unit) {
        while (currentCoroutineContext().isActive) {
            val value = withContext(RustDispatchers.events) { RustCoreBridge.next(stream) }
            if (value.isNotEmpty()) consume(value)
        }
    }

    suspend fun publish(event: HostEvent): Int {
        val capabilities = withTimeoutOrNull(5_000) { capabilities.filterNotNull().first() } ?: error("Rust host event channel unavailable")
        require(capabilities.accepts(event.type, event.data)) { "${event.type} is not a host-produced event" }
        val error = withContext(RustDispatchers.requests) {
            when (val data = event.data) {
                is HostEventData.Text -> RustCoreBridge.send("text", JsonHelper.jsonEncode(HostEventPacket(event.type.name, data.value)), byteArrayOf())
                is HostEventData.Binary -> RustCoreBridge.send("binary", event.type.name, data.value)
            }
        }
        require(error.isEmpty()) { error }
        return com.ismartcoding.plain.features.session.onlineClientIds.value.size
    }
}