package com.ismartcoding.plain.network

import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readAvailable
import kotlinx.coroutines.channels.ReceiveChannel

internal const val STREAM_BUFFER_SIZE = 64 * 1024

suspend fun ByteReadChannel.copyTo(write: suspend (buffer: ByteArray, length: Int) -> Unit): Long {
    val buffer = ByteArray(STREAM_BUFFER_SIZE)
    var total = 0L
    while (true) {
        val read = readAvailable(buffer)
        if (read <= 0) break
        write(buffer, read)
        total += read
    }
    return total
}

class TestHttpRequest(
    val method: String,
    val url: String,
    val headers: Map<String, String> = emptyMap(),
    val body: ByteArray? = null,
    val contentType: String? = null,
)

class TestWsFrame(val text: String?, val binary: ByteArray?)

interface TestWebSocketSession {
    val incoming: ReceiveChannel<TestWsFrame>

    suspend fun sendBinary(data: ByteArray)

    suspend fun sendText(text: String)

    fun close()
}

class TestHttpResponse(
    val status: HttpStatusCode,
    val url: String,
    val headers: Map<String, List<String>>,
    val channel: ByteReadChannel,
    private val onClose: (() -> Unit)? = null,
) : AutoCloseable {
    fun isOk(): Boolean = status == HttpStatusCode.OK

    fun isSuccess(): Boolean = status.isSuccess

    suspend fun bodyAsBytes(): ByteArray {
        val chunks = ArrayList<ByteArray>()
        channel.copyTo { buffer, length -> chunks.add(buffer.copyOf(length)) }
        val out = ByteArray(chunks.sumOf { it.size })
        var pos = 0
        for (chunk in chunks) {
            chunk.copyInto(out, pos)
            pos += chunk.size
        }
        return out
    }

    suspend fun bodyAsText(): String = bodyAsBytes().decodeToString()

    fun header(name: String): String? =
        headers.entries.firstOrNull { it.key.equals(name, ignoreCase = true) }?.value?.firstOrNull()

    override fun toString(): String = "$status $url"

    override fun close() {
        onClose?.invoke()
    }
}

interface TestHttpClient : AutoCloseable {
    suspend fun request(req: TestHttpRequest): TestHttpResponse

    suspend fun <T> webSocket(
        url: String,
        headers: Map<String, String> = emptyMap(),
        block: suspend (TestWebSocketSession) -> T,
    ): T

    override fun close()
}

suspend fun TestHttpClient.request(
    method: String,
    url: String,
    headers: Map<String, String> = emptyMap(),
): TestHttpResponse = request(TestHttpRequest(method, url, headers))

suspend fun TestHttpClient.get(
    url: String,
    headers: Map<String, String> = emptyMap(),
): TestHttpResponse = request(TestHttpRequest("GET", url, headers))

suspend fun TestHttpClient.post(
    url: String,
    body: ByteArray,
    contentType: String? = null,
    headers: Map<String, String> = emptyMap(),
): TestHttpResponse = request(TestHttpRequest("POST", url, headers, body, contentType))

suspend fun TestHttpClient.postText(
    url: String,
    body: String,
    contentType: String? = null,
    headers: Map<String, String> = emptyMap(),
): TestHttpResponse = request(TestHttpRequest("POST", url, headers, body.encodeToByteArray(), contentType))

sealed class TestHttpClientSpec {
    data class Local(val timeoutSeconds: Int = 30) : TestHttpClientSpec()
    data object HttpHost : TestHttpClientSpec()
}
