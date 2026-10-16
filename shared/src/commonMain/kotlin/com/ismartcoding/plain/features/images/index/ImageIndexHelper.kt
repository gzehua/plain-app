package com.ismartcoding.plain.features.images.index

import com.ismartcoding.plain.rust.PlainRust
import com.ismartcoding.plain.rust.event.*
import com.ismartcoding.plain.rust.host.*
import com.ismartcoding.plain.rust.bridge.*
import com.ismartcoding.plain.rust.http.*
import com.ismartcoding.plain.features.feeds.*
import com.ismartcoding.plain.ui.feeds.*
import com.ismartcoding.plain.features.files.*
import com.ismartcoding.plain.ui.files.*
import com.ismartcoding.plain.features.notes.*
import com.ismartcoding.plain.ui.notes.*
import com.ismartcoding.plain.features.tags.*
import com.ismartcoding.plain.ui.tags.*
import com.ismartcoding.plain.rust.request.*
import kotlinx.serialization.json.*

object ImageIndexHelper {
    fun startAsync(force: Boolean) = request { start(force) }
    fun cancelAsync() = request { cancel() }
    private fun request(operation: suspend () -> JsonObject) {
        com.ismartcoding.plain.lib.coIO {
            try { operation() }
            catch (cancelled: kotlinx.coroutines.CancellationException) { throw cancelled }
            catch (error: Exception) {
                com.ismartcoding.plain.features.images.search.ImageSearchManager.setIndexError(error.message ?: "Image index request failed")
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
