package com.ismartcoding.plain.features.imageindex

import com.ismartcoding.plain.rust.PlainRust
import com.ismartcoding.plain.rust.event.*
import com.ismartcoding.plain.rust.host.*
import com.ismartcoding.plain.rust.bridge.*
import com.ismartcoding.plain.rust.http.*
import com.ismartcoding.plain.rust.content.*
import kotlinx.serialization.json.*

object ImageIndexHelper {
    fun startAsync(force: Boolean) = request { start(force) }
    fun cancelAsync() = request { cancel() }
    private fun request(operation: suspend () -> JsonObject) {
        com.ismartcoding.plain.lib.coIO {
            try { operation() }
            catch (cancelled: kotlinx.coroutines.CancellationException) { throw cancelled }
            catch (error: Exception) {
                com.ismartcoding.plain.ai.ImageSearchManager.setIndexError(error.message ?: "Image index request failed")
                com.ismartcoding.plain.lib.logcat.LogCat.e("Image index", error)
            }
        }
    }

    private const val fields = "version isRunning totalImages indexedImages skippedImages errorMessage"
    suspend fun status(): JsonObject = PlainRust.mainQuery("imageIndexStatus { $fields }").getValue("imageIndexStatus").jsonObject
    suspend fun start(force: Boolean): JsonObject = PlainRust.mainMutate("startImageIndexTask(force: $force) { $fields }").getValue("startImageIndexTask").jsonObject
    suspend fun selected(ids: Collection<String>): JsonObject = PlainRust.mainMutate("indexSelectedImages(ids: ${gqlIds(ids)}) { $fields }").getValue("indexSelectedImages").jsonObject
    suspend fun cancel(): JsonObject = PlainRust.mainMutate("cancelImageIndexTask { $fields }").getValue("cancelImageIndexTask").jsonObject
}
