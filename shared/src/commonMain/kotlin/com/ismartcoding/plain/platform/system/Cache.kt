package com.ismartcoding.plain.platform.system

import com.ismartcoding.plain.rust.graphql.RustGraphqlApi

import com.ismartcoding.plain.network.request

import com.ismartcoding.plain.rust.RustRuntime
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long

fun appCacheDir(): String = RustRuntime.cacheDir
fun videoCacheDir(): String = RustRuntime.videoCacheDir
fun imageCacheDir(): String = RustRuntime.imageCacheDir

suspend fun getCacheSize(): Long = RustGraphqlApi.query("appCacheSize").getValue("appCacheSize").jsonPrimitive.long

suspend fun clearCacheAsync() {
    check(RustGraphqlApi.mutate("clearAppCache", longRunning = true).getValue("clearAppCache").jsonPrimitive.boolean)
}
