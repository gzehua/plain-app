package com.ismartcoding.plain.features.audio

import com.ismartcoding.plain.rust.graphql.AppSubscriptions
import com.ismartcoding.plain.rust.graphql.changes
import com.ismartcoding.plain.graphql.Query
import com.ismartcoding.plain.rust.graphql.RustGraphqlApi
import com.ismartcoding.plain.models.AudioOverview
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.platform.system.DataType
import com.ismartcoding.plain.rust.graphql.SubscriptionUpdate
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.filter

object AudioOverviewClient {
    suspend fun read(): AudioOverview = JsonHelper.jsonDecodeFromElement(
        RustGraphqlApi.graphql(Query.audioOverview()).getValue("audioOverview"),
    )

    val updates get() = merge(
        AppSubscriptions.contentChanged.changes(),
        AppSubscriptions.mediaLibraryUpdated.filter(::audioLibraryChangeRefreshes).map { Unit },
        AppSubscriptions.mediaScanProgress.filter(::audioScanChangeRefreshes).map { Unit },
        AppSubscriptions.audioLibraryChanged.changes(),
    )
}

internal fun audioLibraryChangeRefreshes(update: SubscriptionUpdate<com.ismartcoding.plain.api.graphql.models.MediaLibraryChange>): Boolean =
    update is SubscriptionUpdate.Attached || update is SubscriptionUpdate.Data && DataType.AUDIO.value in update.value.types

internal fun audioScanChangeRefreshes(update: SubscriptionUpdate<com.ismartcoding.plain.api.graphql.models.MediaScanProgress>): Boolean =
    update is SubscriptionUpdate.Attached || update is SubscriptionUpdate.Data && update.value.state in setOf("IDLE", "FAILED", "STOPPED", "PAUSED")
