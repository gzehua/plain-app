package com.ismartcoding.plain.rust.host.provider

import com.ismartcoding.plain.api.graphql.models.Notification as AppNotification
import com.ismartcoding.plain.api.graphql.models.MediaBucket
import com.ismartcoding.plain.rust.PlainRust
import com.ismartcoding.plain.rust.request.gql
import com.ismartcoding.plain.platform.system.DataType
import com.ismartcoding.plain.features.files.FileSortBy
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.api.graphql.models.Package as AppPackage
import kotlinx.serialization.json.*

object RustSystemProviders {

    suspend fun packages(query: String, limit: Int, offset: Int, sortBy: FileSortBy): List<AppPackage> =
        JsonHelper.jsonDecodeFromElement(PlainRust.mainQuery("packages(query: ${gql(query)}, offset: $offset, limit: $limit, sortBy: ${sortBy.name}) { id name type version path size installedAt updatedAt certs { issuer subject serialNumber validFrom validTo } }").getValue("packages"))
    suspend fun packageCount(query: String): Int = PlainRust.mainQuery("packageCount(query: ${gql(query)})").getValue("packageCount").jsonPrimitive.int
    suspend fun notifications(query: String, offset: Int, limit: Int): List<AppNotification> =
        JsonHelper.jsonDecodeFromElement(PlainRust.mainQuery("notifications(query: ${gql(query)}, offset: $offset, limit: $limit) { id onlyOnce isClearable appId appName postedAt silent title body actions replyActions }").getValue("notifications"))
    suspend fun notificationCount(query: String): Int = PlainRust.mainQuery("notificationCount(query: ${gql(query)})").getValue("notificationCount").jsonPrimitive.int

    suspend fun mediaBuckets(dataType: DataType): List<MediaBucket> =
        JsonHelper.jsonDecodeFromElement(PlainRust.mainQuery("mediaBuckets(type: ${dataType.name}) { id name itemCount size topItemPaths }").getValue("mediaBuckets"))
}
