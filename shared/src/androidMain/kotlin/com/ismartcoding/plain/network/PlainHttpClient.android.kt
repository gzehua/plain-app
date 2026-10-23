package com.ismartcoding.plain.network

import com.ismartcoding.plain.platform.media.call

import com.ismartcoding.plain.network.OkHttpClientFactory
import com.ismartcoding.plain.network.httpLogSink
import io.ktor.utils.io.ByteChannel
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.close
import io.ktor.utils.io.writeFully
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.delay
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import okio.ByteString.Companion.toByteString
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import java.io.IOException
import okhttp3.Callback

/**
 * OkHttp-backed [PlainHttpClient]. Every spec maps to a dedicated OkHttpClient;
 * no HTTP client from ktor is involved on Android.
 */
internal class OkHttpPlainClient(
    private val client: OkHttpClient,
    private val boundedWs: Boolean = false,
) : PlainHttpClient {
    override suspend fun request(req: PlainRequest): PlainResponse {
        val call = client.newCall(req.toOkHttpRequest())
        val response = call.await()
        val headers = response.headers.toMultimap()
        val stream = response.body?.byteStream() ?: emptyStream()
        val channel = stream.toByteReadChannel { response.close() }
        return PlainResponse(
            status = HttpStatusCode(response.code),
            url = req.url,
            headers = headers,
            channel = channel,
            onClose = { response.close() },
        )
    }

    override suspend fun <T> webSocket(
        url: String,
        headers: Map<String, String>,
        block: suspend (PlainWebSocketSession) -> T,
    ): T {
        val requestBuilder = Request.Builder().url(url)
        headers.forEach { (k, v) -> requestBuilder.header(k, v) }
        val session = OkHttpWsSession(client, requestBuilder.build(), boundedWs)
        try {
            return block(session)
        } finally {
            session.close()
        }
    }

    override fun close() {
        // Connection pool and dispatcher are shared app-wide; nothing to dispose.
    }

    private fun emptyStream(): InputStream = ByteArrayInputStream(ByteArray(0))
}

private suspend fun Call.await(): Response =
    suspendCancellableCoroutine { cont ->
        enqueue(
            object : Callback {
                override fun onResponse(call: Call, response: Response) {
                    if (cont.isActive) cont.resume(response)
                }

                override fun onFailure(call: Call, e: IOException) {
                    if (cont.isActive) cont.resumeWithException(e)
                }
            },
        )
        cont.invokeOnCancellation { cancel() }
    }

/** Streams an [InputStream] into a ktor-io [ByteReadChannel] on the IO dispatcher. */
private fun InputStream.toByteReadChannel(onClose: () -> Unit): ByteReadChannel {
    val input = this
    val channel = ByteChannel(autoFlush = true)
    CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
        val buf = ByteArray(STREAM_BUFFER_SIZE)
        try {
            while (true) {
                val n = input.read(buf)
                if (n <= 0) break
                channel.writeFully(buf, 0, n)
            }
            channel.flush()
            channel.close()
        } catch (e: Exception) {
            channel.close(e)
        } finally {
            onClose()
        }
    }
    return channel
}

internal class OkHttpWsSession(
    client: OkHttpClient,
    request: Request,
    private val bounded: Boolean = false,
) : WebSocketListener(), PlainWebSocketSession {
    private val channel = Channel<PlainWsFrame>(if (bounded) 4 else Channel.UNLIMITED)
    @Volatile private var closed = false
    private val ws: WebSocket = client.newWebSocket(request, this)

    override val incoming = channel

    override fun onMessage(webSocket: WebSocket, text: String) {
        if (bounded) channel.trySendBlocking(PlainWsFrame(text, null)) else channel.trySend(PlainWsFrame(text, null))
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        if (bounded) channel.trySendBlocking(PlainWsFrame(null, bytes.toByteArray())) else channel.trySend(PlainWsFrame(null, bytes.toByteArray()))
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        closed = true
        channel.close()
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        closed = true
        channel.close(t)
    }

    override suspend fun sendBinary(data: ByteArray) {
        if (bounded) awaitCapacity()
        check(ws.send(data.toByteString())) { "WebSocket send failed" }
    }

    override suspend fun sendText(text: String) {
        if (bounded) awaitCapacity()
        check(ws.send(text)) { "WebSocket send failed" }
    }

    private suspend fun awaitCapacity() {
        while (!closed && ws.queueSize() > 256 * 1024) delay(5)
        check(!closed) { "WebSocket closed" }
    }

    override fun close() {
        closed = true
        ws.close(1000, null)
        channel.close()
    }
}

internal fun PlainRequest.toOkHttpRequest(): Request {
    val builder = Request.Builder().url(url)
    headers.forEach { (k, v) -> builder.header(k, v) }
    val mediaType = contentType?.toMediaTypeOrNull()
    val body: RequestBody? = body?.toRequestBody(mediaType)
    return builder.method(method, body).build()
}

internal object SharedOkHttpClients {
    val browser: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .callTimeout(PlainHttpTimeouts.BROWSER_MS, TimeUnit.MILLISECONDS)
            .cookieJar(InMemoryCookieJar)
            .addInterceptor(browserHeaders())
            .addInterceptor(headerLoggingInterceptor())
            .build()
    }

    val unsafe: OkHttpClient by lazy { OkHttpClientFactory.createUnsafeOkHttpClient() }

    val host: OkHttpClient by lazy {
        unsafe.newBuilder()
            .connectTimeout(500, TimeUnit.MILLISECONDS)
            .pingInterval(15, TimeUnit.SECONDS)
            .build()
    }
}

internal fun createOkHttpPlainClient(spec: PlainHttpClientSpec): PlainHttpClient =
    when (spec) {
        is PlainHttpClientSpec.Local -> OkHttpPlainClient(
            SharedOkHttpClients.unsafe.newBuilder()
                .connectTimeout(spec.timeoutSeconds.toLong(), TimeUnit.SECONDS)
                .readTimeout(spec.timeoutSeconds.toLong(), TimeUnit.SECONDS)
                .writeTimeout(spec.timeoutSeconds.toLong(), TimeUnit.SECONDS)
                .build(),
        )
        PlainHttpClientSpec.Browser -> OkHttpPlainClient(SharedOkHttpClients.browser)
        PlainHttpClientSpec.HttpHost -> OkHttpPlainClient(SharedOkHttpClients.host, boundedWs = true)
    }

private fun browserHeaders() = Interceptor { chain ->
    chain.proceed(
        chain.request().newBuilder()
            .header("accept", "*/*")
            .header("User-Agent", BROWSER_USER_AGENT)
            .build(),
    )
}

private fun headerLoggingInterceptor() = Interceptor { chain ->
    val request = chain.request()
    val sb = StringBuilder("HTTP request: ${request.method} ${request.url}")
    request.headers.forEach { (name, value) -> sb.append("\n> $name: $value") }
//    httpLogSink.log(sb.toString())
    val response = chain.proceed(request)
    val rsb = StringBuilder("HTTP response: ${response.code} ${request.url}")
    response.headers.forEach { (name, value) -> rsb.append("\n< $name: $value") }
//    httpLogSink.log(rsb.toString())
    response
}

/** In-memory cookie jar replacing ktor's HttpCookies plugin for the Browser spec. */
private object InMemoryCookieJar : CookieJar {
    private val store = HashMap<HttpUrl, List<Cookie>>()

    @Synchronized
    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        store[url] = cookies
    }

    @Synchronized
    override fun loadForRequest(url: HttpUrl): List<Cookie> =
        store.values.flatten().filter { it.matches(url) }
}

actual fun createPlainHttpClient(spec: PlainHttpClientSpec): PlainHttpClient = createOkHttpPlainClient(spec)
