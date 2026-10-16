package com.ismartcoding.plain.rust.host

import com.ismartcoding.plain.app.events.PermissionsResultEvent
import com.ismartcoding.plain.lib.Channel
import com.ismartcoding.plain.lib.logcat.LogCat
import com.ismartcoding.plain.platform.media.audioCatalogChanges
import com.ismartcoding.plain.platform.permissions.Permission
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.serialization.Serializable

@Serializable
internal data class MediaCatalogChange(val types: List<Int>)

internal suspend fun forwardAudioCatalogChanges(changes: Flow<Unit>, publish: suspend () -> Unit) {
    changes.conflate().collect { publish() }
}

@OptIn(ExperimentalCoroutinesApi::class)
internal fun audioCatalogNotifications(): Flow<Unit> = merge(
    flowOf(Unit),
    Channel.sharedFlow.filterIsInstance<PermissionsResultEvent>()
        .filter { it.has(Permission.READ_MEDIA_AUDIO) || it.has(Permission.WRITE_EXTERNAL_STORAGE) }
        .map { Unit },
).flatMapLatest {
    audioCatalogChanges().catch { error -> LogCat.e("Audio catalog observer registration", error) }
}
