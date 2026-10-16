package com.ismartcoding.plain.platform.system

import com.ismartcoding.plain.api.graphql.models.File as FileEntry

import com.ismartcoding.plain.platform.system.DataType
import com.ismartcoding.plain.rust.host.provider.MediaBucketItemFacts

expect suspend fun deleteSystemProviderFacts(provider: DataType, ids: Set<String>): Set<String>

expect suspend fun systemSmsFacts(method: String, params: kotlinx.serialization.json.JsonObject): kotlinx.serialization.json.JsonElement

expect suspend fun mediaBucketItemFacts(dataType: DataType, plan: com.ismartcoding.plain.rust.host.provider.ProviderPlan): List<MediaBucketItemFacts>

expect suspend fun fileIndexId(path: String): String?
expect suspend fun fileIndexRows(plan: com.ismartcoding.plain.rust.host.provider.ProviderPlan, limit: Int, offset: Int, sortBy: com.ismartcoding.plain.features.files.FileSortBy): List<com.ismartcoding.plain.api.graphql.models.File>
expect suspend fun fileIndexCount(plan: com.ismartcoding.plain.rust.host.provider.ProviderPlan): Int
