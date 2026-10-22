package com.ismartcoding.plain.rust

import kotlinx.serialization.Serializable

import com.ismartcoding.plain.rust.bridge.RustCoreBridge
import com.ismartcoding.plain.rust.bridge.RustDispatchers
import com.ismartcoding.plain.rust.event.HostEventCapabilities
import com.ismartcoding.plain.rust.event.HostEventPacket
import com.ismartcoding.plain.rust.event.RustEvent
import com.ismartcoding.plain.rust.event.RustEventProjection
import com.ismartcoding.plain.rust.host.RustHostApi
import com.ismartcoding.plain.rust.request.ApiSession
import com.ismartcoding.plain.rust.request.GraphqlRequest
import com.ismartcoding.plain.features.settings.AppUpdateDownload
import com.ismartcoding.plain.app.events.*
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.lib.logcat.LogCat
import com.ismartcoding.plain.rust.http.PlainHttpClientSpec
import com.ismartcoding.plain.rust.http.createPlainHttpClient
import com.ismartcoding.plain.rust.http.postText
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

object PlainRust {
    private val client by lazy { createPlainHttpClient(PlainHttpClientSpec.Local()) }
    private val transferClient by lazy { createPlainHttpClient(PlainHttpClientSpec.Local(15 * 60)) }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val incoming = MutableSharedFlow<RustEvent>(extraBufferCapacity = 64)
    val events: SharedFlow<RustEvent> = incoming.asSharedFlow()
    internal val hostRequests = Channel<String>(32)
    private var started = false
    private val capabilities = MutableStateFlow<HostEventCapabilities?>(null)

    suspend fun mainQuery(selection: String, session: ApiSession? = null): JsonObject =
        data("query { $selection }", session)

    suspend fun mainMutate(selection: String, session: ApiSession? = null, longRunning: Boolean = false): JsonObject =
        data("mutation { $selection }", session, longRunning = longRunning)

    suspend fun mainGraphql(document: String, variables: Map<String, JsonElement> = emptyMap(), session: ApiSession? = null, longRunning: Boolean = false): JsonObject =
        data(document, session, variables = variables, longRunning = longRunning)

    private suspend fun data(
        document: String,
        session: ApiSession?,
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
        session: ApiSession,
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
        val envelope = JsonHelper.jsonDecodeFromElement<GraphqlEnvelope>(response)
        check(envelope.errors.isNullOrEmpty()) { envelope.errors?.joinToString { it.message ?: "Rust API error" } ?: "Rust API error" }
        return requireNotNull(envelope.data) { "Rust API response missing data" }
    }

    // ── Core lifecycle and native streams ────────────────────────────────

    internal fun initialize(dataDir: String, token: String, config: String): String = RustCoreBridge.start(dataDir, token, config)
    internal fun stopHttp() = RustCoreBridge.stop()

    internal fun start() {
        check(!started) { "PlainRust already started" }
        capabilities.value = JsonHelper.jsonDecode(RustCoreBridge.next("capabilities"))
        started = true
        RustHostApi.start(scope)
        scope.launch { pump("host", closed = { hostRequests.cancel() }) { hostRequests.send(it) } }
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

    private suspend fun pump(stream: String, closed: () -> Unit = {}, consume: suspend (String) -> Unit) {
        com.ismartcoding.plain.rust.bridge.readRustStream(
            next = { withContext(RustDispatchers.events) { RustCoreBridge.next(stream) } },
            consume = consume,
            closed = closed,
        )
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
        return com.ismartcoding.plain.features.sessions.onlineClientIds.value.size
    }

    internal suspend inline fun <reified T : Any> call(crossinline request: () -> String): T = withContext(RustDispatchers.requests) {
        val reply = JsonHelper.jsonDecode<NativeManagementReply<T>>(request())
        reply.error?.let { error(it) }
        requireNotNull(reply.value) { "Native management response missing value" }
    }

    internal suspend fun regenerateTlsCertificate(): List<Int> = call { RustCoreBridge.regenerateTlsCertificate() }
    internal suspend fun importTlsCertificatePem(certificatePem: String, privateKeyPem: String): List<Int> = call { RustCoreBridge.importTlsCertificatePem(certificatePem, privateKeyPem) }
    internal suspend fun importTlsCertificatePkcs12(data: ByteArray, password: String): List<Int> = call { RustCoreBridge.importTlsCertificatePkcs12(data, password) }
    internal suspend fun createSession(name: String): com.ismartcoding.plain.features.sessions.CreatedSession = call { RustCoreBridge.createSession(name) }
    internal suspend fun confirmWebLogin(requestId: String, allowed: Boolean): Boolean = call { RustCoreBridge.confirmWebLogin(requestId, allowed) }
    internal suspend fun stageAppRestore(path: String): Boolean = call { RustCoreBridge.stageAppRestore(path) }
    internal suspend fun exportAppBackup(): String = call { RustCoreBridge.exportAppBackup() }
    internal suspend fun createCrashReport(report: String): String = call { RustCoreBridge.createCrashReport(report) }
    internal suspend fun systemSettings(): com.ismartcoding.plain.preferences.SystemSettings = call { RustCoreBridge.systemSettings() }
    internal suspend fun userSettings(): JsonObject = call { RustCoreBridge.userSettings() }
    internal suspend fun toggleNotificationApp(packageName: String): Boolean = call { RustCoreBridge.toggleNotificationApp(packageName) }
    internal suspend fun verifyAdbToken(token: String): Boolean = call { RustCoreBridge.verifyAdbToken(token) }
    internal suspend fun startAppUpdateDownload(): AppUpdateDownload = call { RustCoreBridge.startAppUpdateDownload() }
    internal suspend fun cancelAppUpdateDownload(): AppUpdateDownload = call { RustCoreBridge.cancelAppUpdateDownload() }
    internal suspend fun checkAppUpdate(automatic: Boolean): String = call { RustCoreBridge.checkAppUpdate(automatic) }
}

@Serializable
internal data class NativeManagementReply<T>(val value: T? = null, val error: String? = null)

@Serializable
private data class GraphqlEnvelope(val data: JsonObject? = null, val errors: List<GraphqlError>? = null)

@Serializable
private data class GraphqlError(val message: String? = null)
