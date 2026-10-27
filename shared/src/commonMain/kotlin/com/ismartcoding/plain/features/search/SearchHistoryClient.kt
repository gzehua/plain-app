package com.ismartcoding.plain.features.search

import com.ismartcoding.plain.features.settings.SavedValuesClient

object SearchHistoryClient {
    suspend fun read(): List<String> = SavedValuesClient.get("recent_searches", emptyList())

    suspend fun record(term: String) = SavedValuesClient.set("recent_searches", (listOf(term) + read().filterNot { it.equals(term, ignoreCase = true) }).take(10))

    suspend fun remove(term: String) = SavedValuesClient.set("recent_searches", read().filterNot { it.equals(term, ignoreCase = true) })

    suspend fun clear() = SavedValuesClient.set("recent_searches", emptyList<String>())
}
