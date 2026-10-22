package com.ismartcoding.plain.platform.system

import com.ismartcoding.plain.rust.graphql.RustGraphqlApi

import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long

suspend fun getCacheSize(): Long = RustGraphqlApi.query("appCacheSize").getValue("appCacheSize").jsonPrimitive.long

suspend fun clearCacheAsync() {
    check(RustGraphqlApi.mutate("clearAppCache", longRunning = true).getValue("clearAppCache").jsonPrimitive.boolean)
}
