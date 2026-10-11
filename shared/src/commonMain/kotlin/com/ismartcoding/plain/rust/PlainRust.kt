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

/**
 * The single entry point into the shared Rust core.
 *
 * Transport follows the *target*, never the operation or the schema: no [session]
 * means this device and the document runs in process over the native bridge; an
 * explicit [session] is another device and the same document goes over HTTP.
 * Nothing retries the other transport after a failure — a native refusal is
 * reported.
 *
 * A core serves three schemas, so the entry points name the endpoint rather than
 * just "query". All three are addressable on either transport; main is the only
 * one this app needs against itself today.
 */
object PlainRust {
    /** The GraphQL endpoint a remote core serves for each schema. */
    enum class Endpoint(val path: String) {
        Main("/graphql"),
        Peer("/peer_graphql"),
        Guest("/guest_graphql"),
    }

    private const val HOST_API = true

    private val client by lazy { createPlainHttpClient(PlainHttpClientSpec.Local()) }
    private val transferClient by lazy { createPlainHttpClient(PlainHttpClientSpec.Local(15 * 60)) }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val incoming = MutableSharedFlow<RustEvent>(extraBufferCapacity = 64)
    val events: SharedFlow<RustEvent> = incoming.asSharedFlow()
    internal val hostRequests = Channel<String>(32)
    private var started = false
    private val capabilities = MutableStateFlow<HostEventCapabilities?>(null)

    // ── Main schema: this device over the bridge, another device over HTTP ──

    suspend fun mainQuery(selection: String, session: ContentApiSession? = null): JsonObject =
        data("query { $selection }", Endpoint.Main, session)

    suspend fun mainMutate(selection: String, session: ContentApiSession? = null, longRunning: Boolean = false): JsonObject =
        data("mutation { $selection }", Endpoint.Main, session, longRunning = longRunning)

    suspend fun mainGraphql(document: String, variables: Map<String, JsonElement> = emptyMap(), session: ContentApiSession? = null): JsonObject =
        data(document, variables = variables, endpoint = Endpoint.Main, session = session)

    // ── Peer and guest schemas: another device only ────────────────────────

    suspend fun peerQuery(selection: String, session: ContentApiSession): JsonObject =
        remoteData(Endpoint.Peer, "query { $selection }", session)

    suspend fun peerMutate(selection: String, session: ContentApiSession): JsonObject =
        remoteData(Endpoint.Peer, "mutation { $selection }", session)

    suspend fun guestQuery(selection: String, session: ContentApiSession): JsonObject =
        remoteData(Endpoint.Guest, "query { $selection }", session)

    suspend fun guestMutate(selection: String, session: ContentApiSession): JsonObject =
        remoteData(Endpoint.Guest, "mutation { $selection }", session)

    // ── Execution ─────────────────────────────────────────────────────────

    /** Runs the document and unwraps `data`, raising the first GraphQL error. */
    private suspend fun data(
        document: String,
        endpoint: Endpoint,
        session: ContentApiSession?,
        variables: Map<String, JsonElement> = emptyMap(),
        operationName: String? = null,
        longRunning: Boolean = false,
    ): JsonObject = unwrap(execute(document, variables, operationName, endpoint, session, longRunning))

    /** Returns the complete GraphQL response, keeping `data`/`errors`/`extensions`. */
    internal suspend fun execute(
        document: String,
        variables: Map<String, JsonElement> = emptyMap(),
        operationName: String? = null,
        endpoint: Endpoint = Endpoint.Main,
        session: ContentApiSession? = null,
        longRunning: Boolean = false,
    ): JsonObject = if (session == null) {
        check(endpoint == Endpoint.Main) { "The native bridge does not expose the $endpoint schema yet" }
        native(document, variables, operationName)
    } else {
        remote(document, variables, operationName, endpoint, session, longRunning)
    }

    /**
     * The peer and guest schemas accept an XChaCha20 envelope with `c-id`/`c-cid`
     * headers, not the plain JSON body main uses. Kotlin has no XChaCha20 yet, so
     * these entries stay closed rather than posting a body the endpoint rejects.
     */
    private suspend fun remoteData(endpoint: Endpoint, document: String, session: ContentApiSession): JsonObject =
        throw UnsupportedOperationException(
            "$endpoint requires the XChaCha20 peer envelope, which the Kotlin side cannot produce yet",
        )

    private suspend fun native(document: String, variables: Map<String, JsonElement>, operationName: String?): JsonObject =
        withContext(RustDispatchers.requests) {
            JsonHelper.jsonDecode(RustCoreBridge.graphql(JsonHelper.jsonEncode(GraphqlRequest(document, variables, operationName)), HOST_API))
        }

    /**
     * Remote GraphQL authenticates by mode, not by header alone: a paired device
     * sends `c-id` and an XChaCha20 body with no `Authorization`, while a web
     * console `CUSTOM` session sends a Bearer header and a plaintext body.
     * Kotlin has no XChaCha20 yet, so the paired mode stays closed rather than
     * posting a body the endpoint would reject.
     */
    private suspend fun remote(
        document: String,
        variables: Map<String, JsonElement>,
        operationName: String?,
        endpoint: Endpoint,
        session: ContentApiSession,
        longRunning: Boolean,
    ): JsonObject {
        check(session.sessionKey == null) {
            "A paired session needs the XChaCha20 envelope, which the Kotlin side cannot produce yet"
        }
        val body = JsonHelper.jsonEncode(GraphqlRequest(document, variables, operationName))
        val response = (if (longRunning) transferClient else client)
            .postText("${session.baseUrl}${endpoint.path}", body, "application/json", session.headers())
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

    internal fun initialize(databasePath: String, token: String, config: String): String = RustCoreBridge.start(databasePath, token, config)
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
        val capabilities = withTimeoutOrNull(5_000) { capabilities.filterNotNull().first() } ?: return 0
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