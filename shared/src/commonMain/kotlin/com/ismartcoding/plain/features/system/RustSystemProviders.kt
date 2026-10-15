package com.ismartcoding.plain.features.system

import com.ismartcoding.plain.rust.request.RustContentApi
import com.ismartcoding.plain.data.DNotification
import com.ismartcoding.plain.data.DMediaBucket
import com.ismartcoding.plain.enums.DataType
import com.ismartcoding.plain.features.file.FileSortBy
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.platform.DPackageInfo
import kotlinx.serialization.json.*

object RustSystemProviders {

    private suspend fun call(action: String, query: String, offset: Int = 0, limit: Int = 0, sortBy: FileSortBy = FileSortBy.NAME_ASC): JsonObject =
        RustContentApi.postJsonOrThrow("system/providers", buildJsonObject {
            put("action", action); put("query", query); put("offset", offset); put("limit", limit); put("sortBy", sortBy.name)
        })
    suspend fun providerWhere(provider: com.ismartcoding.plain.enums.DataType, query: String, resolvedParentId: String? = null): com.ismartcoding.plain.helpers.ContentWhere {
        val result = RustContentApi.postJsonOrThrow("system/provider-plan", JsonHelper.jsonEncodeToElement(ProviderPlanRequest(provider.name, query, resolvedParentId)).jsonObject)
        return JsonHelper.jsonDecodeFromElement<ProviderPlan>(result).where()
    }
    suspend fun providerWheres(provider: com.ismartcoding.plain.enums.DataType, query: String): List<com.ismartcoding.plain.helpers.ContentWhere> {
        val result = RustContentApi.postJsonOrThrow("system/provider-plan", JsonHelper.jsonEncodeToElement(ProviderPlanRequest(provider.name, query)).jsonObject)
        return JsonHelper.jsonDecodeFromElement<ProviderPlan>(result).wheres()
    }

    suspend fun packages(query: String, limit: Int, offset: Int, sortBy: FileSortBy): List<DPackageInfo> =
        JsonHelper.jsonDecode(call("packages", query, offset, limit, sortBy).getValue("items").toString())
    suspend fun packageCount(query: String): Int = call("packageCount", query).getValue("count").jsonPrimitive.int
    suspend fun notifications(query: String, offset: Int, limit: Int): List<DNotification> =
        JsonHelper.jsonDecode(call("notifications", query, offset, limit).getValue("items").toString())
suspend fun notificationCount(query: String): Int = call("notificationCount", query).getValue("count").jsonPrimitive.int

    suspend fun mediaBuckets(dataType: DataType): List<DMediaBucket> {
        val facts = com.ismartcoding.plain.platform.mediaBucketItemFacts(dataType)
        val response = RustContentApi.postJsonOrThrow("system/media-buckets", buildJsonObject {
            put("dataType", dataType.name)
            put("items", Json.parseToJsonElement(JsonHelper.jsonEncode(facts)))
        })
        return response.getValue("items").jsonArray.map { value ->
            val item = value.jsonObject
            DMediaBucket(
                id = item.getValue("id").jsonPrimitive.content,
                name = item.getValue("name").jsonPrimitive.content,
                itemCount = item.getValue("itemCount").jsonPrimitive.int,
                size = item.getValue("size").jsonPrimitive.long,
                topItems = item.getValue("topItems").jsonArray.map { it.jsonPrimitive.content }.toMutableList(),
            )
        }
    }
}

@kotlinx.serialization.Serializable
private data class ProviderPlanRequest(val provider: String, val query: String, val resolvedParentId: String? = null)
