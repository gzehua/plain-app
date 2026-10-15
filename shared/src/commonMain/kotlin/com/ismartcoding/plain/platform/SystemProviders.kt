package com.ismartcoding.plain.platform

import com.ismartcoding.plain.enums.DataType
import com.ismartcoding.plain.data.DMediaBucketItemFact

expect suspend fun deleteSystemProviderFacts(provider: DataType, ids: Set<String>): Set<String>

expect suspend fun systemSmsFacts(method: String, params: kotlinx.serialization.json.JsonObject): kotlinx.serialization.json.JsonElement

expect suspend fun mediaBucketItemFacts(dataType: DataType): List<DMediaBucketItemFact>

expect suspend fun fileIndexId(path: String): String?
expect suspend fun fileIndexRows(plan: com.ismartcoding.plain.features.system.ProviderPlan, limit: Int, offset: Int, sortBy: com.ismartcoding.plain.features.file.FileSortBy): List<com.ismartcoding.plain.features.file.DFile>
expect suspend fun fileIndexCount(plan: com.ismartcoding.plain.features.system.ProviderPlan): Int
