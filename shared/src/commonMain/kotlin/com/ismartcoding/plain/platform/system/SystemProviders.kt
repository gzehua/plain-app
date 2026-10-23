package com.ismartcoding.plain.platform.system

import com.ismartcoding.plain.models.File as FileEntry

import com.ismartcoding.plain.platform.system.DataType
import com.ismartcoding.plain.rust.host.provider.MediaBucketItemFacts
import com.ismartcoding.plain.features.files.FileSortBy
import com.ismartcoding.plain.rust.host.provider.ProviderPlan
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

expect suspend fun deleteSystemProviderFacts(provider: DataType, ids: Set<String>): Set<String>

expect suspend fun systemSmsFacts(method: String, params: JsonObject): JsonElement

expect suspend fun mediaBucketItemFacts(dataType: DataType, plan: ProviderPlan): List<MediaBucketItemFacts>

expect suspend fun fileIndexId(path: String): String?
expect suspend fun fileIndexRows(plan: ProviderPlan, limit: Int, offset: Int, sortBy: FileSortBy): List<FileEntry>
expect suspend fun fileIndexCount(plan: ProviderPlan): Int
