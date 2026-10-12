package com.ismartcoding.plain.features.imageindex

import com.ismartcoding.plain.rust.PlainRust
import com.ismartcoding.plain.rust.event.*
import com.ismartcoding.plain.rust.host.*
import com.ismartcoding.plain.rust.bridge.*
import com.ismartcoding.plain.rust.http.*
import com.ismartcoding.plain.rust.content.*
import kotlinx.serialization.json.*

object ImageIndexHelper {
    private const val fields = "version isRunning totalImages indexedImages skippedImages errorMessage"
    suspend fun status(): JsonObject = PlainRust.privateQuery("imageHostIndexStatus { $fields }").getValue("imageHostIndexStatus").jsonObject
    suspend fun start(force: Boolean): JsonObject = PlainRust.privateMutate("imageHostStartIndex(force: $force) { $fields }").getValue("imageHostStartIndex").jsonObject
    suspend fun selected(ids: Collection<String>): JsonObject = PlainRust.privateMutate("imageHostIndexSelected(ids: ${gqlIds(ids)}) { $fields }").getValue("imageHostIndexSelected").jsonObject
    suspend fun cancel(): JsonObject = PlainRust.privateMutate("imageHostCancelIndex { $fields }").getValue("imageHostCancelIndex").jsonObject
}
