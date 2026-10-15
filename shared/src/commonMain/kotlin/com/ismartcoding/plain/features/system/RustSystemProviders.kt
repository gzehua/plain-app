package com.ismartcoding.plain.features.system

import com.ismartcoding.plain.rust.PlainRust
import com.ismartcoding.plain.rust.content.gql
import com.ismartcoding.plain.data.DNotification
import com.ismartcoding.plain.data.DMediaBucket
import com.ismartcoding.plain.enums.DataType
import com.ismartcoding.plain.features.file.FileSortBy
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.platform.DPackageInfo
import kotlinx.serialization.json.*

object RustSystemProviders {

    suspend fun packages(query: String, limit: Int, offset: Int, sortBy: FileSortBy): List<DPackageInfo> =
        JsonHelper.jsonDecodeFromElement(PlainRust.mainQuery("packages(query: ${gql(query)}, offset: $offset, limit: $limit, sortBy: ${sortBy.name}) { id name type version path size installedAt updatedAt certs { issuer subject serialNumber validFrom validTo } }").getValue("packages"))
    suspend fun packageCount(query: String): Int = PlainRust.mainQuery("packageCount(query: ${gql(query)})").getValue("packageCount").jsonPrimitive.int
    suspend fun notifications(query: String, offset: Int, limit: Int): List<DNotification> =
        JsonHelper.jsonDecodeFromElement(PlainRust.mainQuery("notifications(query: ${gql(query)}, offset: $offset, limit: $limit) { id onlyOnce isClearable appId appName time: postedAt silent title body actions replyActions }").getValue("notifications"))
    suspend fun notificationCount(query: String): Int = PlainRust.mainQuery("notificationCount(query: ${gql(query)})").getValue("notificationCount").jsonPrimitive.int

    suspend fun mediaBuckets(dataType: DataType): List<DMediaBucket> =
        PlainRust.mainQuery("mediaBuckets(type: ${dataType.name}) { id name itemCount size topItemPaths }").getValue("mediaBuckets").jsonArray.map { value ->
            val item = value.jsonObject
            DMediaBucket(
                id = item.getValue("id").jsonPrimitive.content,
                name = item.getValue("name").jsonPrimitive.content,
                itemCount = item.getValue("itemCount").jsonPrimitive.int,
                size = item.getValue("size").jsonPrimitive.long,
                topItems = item.getValue("topItemPaths").jsonArray.map { it.jsonPrimitive.content }.toMutableList(),
            )
        }

}
