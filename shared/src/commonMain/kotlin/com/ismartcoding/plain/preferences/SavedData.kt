package com.ismartcoding.plain.preferences

import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.rust.graphql.RustGraphqlApi
import kotlinx.serialization.json.JsonNull

object SavedData {
    suspend inline fun <reified T> read(key: String, default: T): T {
        val value = RustGraphqlApi.graphql("query(\$key: String!) { savedValue(key: \$key) }", mapOf("key" to JsonHelper.jsonEncodeToElement(key))).getValue("savedValue")
        return if (value == JsonNull) default else JsonHelper.jsonDecodeFromElement(value)
    }
    suspend inline fun <reified T> write(key: String, value: T) {
        RustGraphqlApi.graphql("mutation(\$key: String!, \$value: JSON!) { setSavedValue(key: \$key, value: \$value) }", mapOf("key" to JsonHelper.jsonEncodeToElement(key), "value" to JsonHelper.jsonEncodeToElement(value)))
    }
    suspend fun recentSearches(): List<String> = read("recent_searches", emptyList())
    suspend fun recordSearch(term: String) = write("recent_searches", (listOf(term) + recentSearches().filterNot { it.equals(term, ignoreCase = true) }).take(10))
    suspend fun removeSearch(term: String) = write("recent_searches", recentSearches().filterNot { it.equals(term, ignoreCase = true) })
    suspend fun recentSaveDirs(): List<String> = read("recent_save_dirs", emptyList())
    suspend fun recordSaveDir(path: String) = write("recent_save_dirs", (listOf(path) + recentSaveDirs().filterNot { it == path }).take(5))
    suspend fun scanHistory(): List<String> = JsonHelper.jsonDecodeFromElement(RustGraphqlApi.graphql("query { scanHistory }").getValue("scanHistory"))
    suspend fun addScan(text: String) { RustGraphqlApi.graphql("mutation(\$text: String!) { addScanHistory(text: \$text) }", mapOf("text" to JsonHelper.jsonEncodeToElement(text))) }
    suspend fun removeScan(text: String) { RustGraphqlApi.graphql("mutation(\$text: String!) { removeScanHistory(text: \$text) }", mapOf("text" to JsonHelper.jsonEncodeToElement(text))) }
}
