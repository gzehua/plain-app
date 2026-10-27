package com.ismartcoding.plain.platform.system

import com.ismartcoding.plain.graphql.Mutation
import com.ismartcoding.plain.graphql.Query
import com.ismartcoding.plain.rust.graphql.RustGraphqlApi

suspend fun getCacheSize(): Long = RustGraphqlApi.execute(Query.appCacheSize())

suspend fun clearCacheAsync() {
    check(RustGraphqlApi.execute(Mutation.clearAppCache()))
}
