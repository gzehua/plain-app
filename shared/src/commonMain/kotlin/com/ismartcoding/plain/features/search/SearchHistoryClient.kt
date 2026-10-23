package com.ismartcoding.plain.features.search

import com.ismartcoding.plain.rust.graphql.RustGraphqlApi

object SearchHistoryClient {
    suspend fun read(): List<String> = RustGraphqlApi.readSavedValue("recent_searches", emptyList())
    suspend fun record(term: String) = RustGraphqlApi.writeSavedValue("recent_searches", (listOf(term) + read().filterNot { it.equals(term, ignoreCase = true) }).take(10))
    suspend fun remove(term: String) = RustGraphqlApi.writeSavedValue("recent_searches", read().filterNot { it.equals(term, ignoreCase = true) })
    suspend fun clear() = RustGraphqlApi.writeSavedValue("recent_searches", emptyList<String>())
}
