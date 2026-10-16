package com.ismartcoding.plain.platform

import com.ismartcoding.plain.rust.request.RustContentApi
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long

fun appCacheDir(): String = RustContentApi.cacheDirectory
fun videoCacheDir(): String = RustContentApi.videoCacheDirectory
fun imageCacheDir(): String = RustContentApi.imageCacheDirectory

suspend fun getCacheSize(): Long = RustContentApi.query("appCacheSize").getValue("appCacheSize").jsonPrimitive.long

suspend fun clearCacheAsync() {
    check(RustContentApi.mutate("clearAppCache", longRunning = true).getValue("clearAppCache").jsonPrimitive.boolean)
}
