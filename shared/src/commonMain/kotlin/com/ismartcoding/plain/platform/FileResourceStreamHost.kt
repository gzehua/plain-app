package com.ismartcoding.plain.platform

import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.lib.logcat.LogCat
import com.ismartcoding.plain.rust.bridge.RustBridgeReply
import com.ismartcoding.plain.rust.bridge.RustCoreBridge
import com.ismartcoding.plain.rust.request.ContentApiSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject

internal object FileResourceStreamHost {
    fun start(scope: CoroutineScope, session: ContentApiSession, params: JsonObject) {
        val id = JsonHelper.jsonDecodeFromElement<ResourceStreamId>(params).id
        scope.launch {
            var completed = false
            try {
                val metadata = JsonHelper.jsonDecode<ResourceStreamRequest>(RustBridgeReply.value(RustCoreBridge.resourceOpen(id)))
                val sink = NativeResourceStreamSink(id)
                try {
                    check(streamContentUri(metadata.path, sink) != null) { "Unable to open content URI" }
                    val error = RustCoreBridge.resourceClose(id, false)
                    check(error.isEmpty()) { error }
                    completed = true
                } finally { sink.close() }
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (error: Exception) { LogCat.e("Native resource stream ended", error) }
            finally { if (!completed) RustCoreBridge.resourceClose(id, true) }
        }
    }
}
