package com.ismartcoding.plain.platform.system

import com.ismartcoding.plain.rust.http.request

import com.ismartcoding.plain.rust.request.RustApi
import com.ismartcoding.plain.rust.request.RustRuntime
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long

fun appCacheDir(): String = RustRuntime.cacheDir
fun videoCacheDir(): String = RustRuntime.videoCacheDir
fun imageCacheDir(): String = RustRuntime.imageCacheDir

suspend fun getCacheSize(): Long = RustApi.query("appCacheSize").getValue("appCacheSize").jsonPrimitive.long

suspend fun clearCacheAsync() {
    check(RustApi.mutate("clearAppCache", longRunning = true).getValue("clearAppCache").jsonPrimitive.boolean)
}
