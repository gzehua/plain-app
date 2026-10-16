package com.ismartcoding.plain.rust.request

import com.ismartcoding.plain.rust.PlainRust
import com.ismartcoding.plain.rust.content.gql
import com.ismartcoding.plain.TempData
import com.ismartcoding.plain.lib.JsonHelper

import com.ismartcoding.plain.events.*
import com.ismartcoding.plain.features.feed.FeedWorkerState
import com.ismartcoding.plain.features.feed.FeedWorkerStatus
import com.ismartcoding.plain.lib.sendEvent
import com.ismartcoding.plain.platform.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.*
import kotlin.concurrent.Volatile

/** A refusal or transport failure from the local Rust core. */
class RustApiException(message: String, cause: Throwable? = null) : Exception(message, cause)

object RustContentApi {
    private val lock = PlatformLock()
    private val client by lazy { createPlainHttpClient(PlainHttpClientSpec.Local()) }
    private val statusLock = Mutex()

    /** Endpoint description of this device's own Rust server, for the REST calls below. */
    @Volatile private var localSession: ContentApiSession? = null
    private val syncStates = MutableStateFlow<Map<String, Pair<String, String>>>(emptyMap())

    internal fun startedSession(): ContentApiSession? = localSession

    internal suspend fun transportSession(): ContentApiSession {
        return checkNotNull(localSession) { "Rust HTTP server is not initialized" }
    }

    val directory: String by lazy { dataDirPath() }
    @Volatile private var imageCachePath: String? = null
    @Volatile private var videoCachePath: String? = null
    val cacheDirectory: String by lazy { cacheDirPath() }
    val videoCacheDirectory: String get() = checkNotNull(videoCachePath) { "Rust video cache is not initialized" }
    val imageCacheDirectory: String get() = checkNotNull(imageCachePath) { "Rust image cache is not initialized" }

    @Volatile internal var httpGeneration: Long = 0L
        private set
    private var lastHttpGeneration: Long = 0L
    private var sessionToken: String? = null
    private var collectorsStarted = false

    private val startupConfig by lazy {
        RustHttpServerConfig(isDebugBuild(), RustWebAssets.ensure(), startupSystemProperties())
    }

    fun start() = lock.withLock {
        if (httpGeneration != 0L) return@withLock
        val token = sessionToken ?: generateChaCha20Key().also { sessionToken = it }
        val config = startupConfig
        val ports = JsonHelper.jsonDecode<RustHttpServerResult>(PlainRust.initialize(
            directory, token, JsonHelper.jsonEncode(config)))
        check(ports.errorCode.isEmpty()) { ports.error }
        require(ports.cacheDir.startsWith('/') && ports.imageCacheDir.startsWith('/') && ports.videoCacheDir.startsWith('/')) { "Rust cache paths required" }
        check(ports.cacheDir == cacheDirectory) { "Rust cache root differs from startup configuration" }
        imageCachePath = ports.imageCacheDir
        videoCachePath = ports.videoCacheDir
        localSession = ContentApiSession(
            "http://127.0.0.1:${ports.httpPort}",
            TempData.clientId,
            token,
        )
        if (!collectorsStarted) {
            PlainRust.start { checkNotNull(localSession) }
            collectorsStarted = true
        }
        kotlinx.coroutines.runBlocking(com.ismartcoding.plain.platform.IODispatcher) { com.ismartcoding.plain.preferences.RustSystemState.refresh() }
        httpGeneration = ports.generation
        lastHttpGeneration = maxOf(lastHttpGeneration, ports.generation)

    }

    internal fun stopHttp() = lock.withLock {
        PlainRust.stopHttp()
        httpGeneration = 0L
    }

    internal fun httpStarted(generation: Long, httpPort: Int) = lock.withLock {
        if (generation <= lastHttpGeneration || localSession == null) return@withLock
        check(httpPort in 1..65535) { "Invalid HTTP port" }
        localSession = ContentApiSession(
            "http://127.0.0.1:$httpPort", TempData.clientId, checkNotNull(sessionToken),
        )
        lastHttpGeneration = generation
        httpGeneration = generation
    }

    internal fun httpFailed(generation: Long): Boolean = lock.withLock {
        if (httpGeneration == 0L || httpGeneration != generation) return@withLock false
        httpGeneration = 0L
        true
    }

    // ── GraphQL: delegated, the transport lives in PlainRust ─────────────

    suspend fun query(selection: String, session: ContentApiSession? = null): JsonObject =
        PlainRust.mainQuery(selection, session)

    suspend fun mutate(selection: String, session: ContentApiSession? = null, longRunning: Boolean = false): JsonObject =
        PlainRust.mainMutate(selection, session, longRunning)

    suspend fun graphql(document: String, variables: Map<String, JsonElement> = emptyMap(), session: ContentApiSession? = null): JsonObject =
        PlainRust.mainGraphql(document, variables, session)

    suspend fun sync(feedId: String) {
        val key = feedId.ifEmpty { "all" }
        mutate("syncFeeds(id: ${if (feedId.isEmpty()) "null" else gql(feedId)})")
        refreshSyncStates()
        val completed = withTimeout(5 * 60 * 1000L) {
            syncStates.first { it[key]?.first?.let { status -> status != "PENDING" } == true }.getValue(key)
        }
        check(completed.first == "COMPLETED") { completed.second.ifEmpty { "Feed sync failed" } }
    }

    internal suspend fun refreshSyncStates() = statusLock.withLock {
        val values = query("feedSyncStates { feedId status error }")["feedSyncStates"]!!.jsonArray
        val states = values.associate { value ->
            val row = value.jsonObject
            row.getValue("feedId").jsonPrimitive.content to (row.getValue("status").jsonPrimitive.content to row.getValue("error").jsonPrimitive.content)
        }
        syncStates.value = states
        withContext(Dispatchers.Main) {
            states.forEach { (id, state) ->
                val status = FeedWorkerStatus.valueOf(state.first)
                FeedWorkerState.statusMap[id] = status
                if (state.second.isEmpty()) FeedWorkerState.errorMap.remove(id) else FeedWorkerState.errorMap[id] = state.second
                sendEvent(HFeedStatusEvent(id, status))
            }
        }
    }

    suspend fun publish(event: HostEvent): Int = PlainRust.publish(event)
}
