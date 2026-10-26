package com.ismartcoding.plain.platform.system

import com.ismartcoding.plain.graphql.Mutation
import com.ismartcoding.plain.graphql.Query
import com.ismartcoding.plain.rust.graphql.RustGraphqlApi
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long

suspend fun getCacheSize(): Long = RustGraphqlApi.graphql(Query.appCacheSize()).getValue("appCacheSize").jsonPrimitive.long

suspend fun clearCacheAsync() {
    check(RustGraphqlApi.graphql(Mutation.clearAppCache()).getValue("clearAppCache").jsonPrimitive.boolean)
}
