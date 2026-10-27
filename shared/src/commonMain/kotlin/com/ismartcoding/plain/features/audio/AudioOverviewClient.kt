package com.ismartcoding.plain.features.audio

import com.ismartcoding.plain.rust.graphql.AppSubscriptions
import com.ismartcoding.plain.rust.graphql.changes
import com.ismartcoding.plain.graphql.Query
import com.ismartcoding.plain.rust.graphql.RustGraphqlApi
import com.ismartcoding.plain.models.AudioOverview
import com.ismartcoding.plain.models.MediaScanState
import com.ismartcoding.plain.platform.system.DataType
import kotlinx.coroutines.flow.merge

object AudioOverviewClient {
    suspend fun read(): AudioOverview =
        RustGraphqlApi.read(Query.audioOverview())

    val updates get() = merge(
        AppSubscriptions.contentChanged.changes(),
        AppSubscriptions.mediaLibraryUpdated.changes { DataType.AUDIO in it.types },
        AppSubscriptions.mediaScanProgress.changes {
            when (it.state) {
                MediaScanState.IDLE, MediaScanState.FAILED, MediaScanState.STOPPED, MediaScanState.PAUSED -> true
                MediaScanState.RUNNING -> false
            }
        },
    )
}
