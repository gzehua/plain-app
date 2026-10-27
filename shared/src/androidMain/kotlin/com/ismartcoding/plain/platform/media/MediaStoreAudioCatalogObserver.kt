package com.ismartcoding.plain.platform.media

import com.ismartcoding.plain.rust.native.RustHostReports
import com.ismartcoding.plain.events.PermissionsResultEvent
import com.ismartcoding.plain.lib.Channel as AppChannel
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.lib.logcat.LogCat
import com.ismartcoding.plain.platform.permissions.Permission
import com.ismartcoding.plain.platform.system.DataType
import com.ismartcoding.plain.rust.host.audio.AudioCatalogObserver
import com.ismartcoding.plain.rust.host.audio.MediaCatalogChange
import com.ismartcoding.plain.rust.host.audio.forwardAudioCatalogChanges
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.*
import android.database.ContentObserver
import android.provider.MediaStore
import com.ismartcoding.plain.appContext
import com.ismartcoding.plain.platform.isQPlus
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow

private fun audioCatalogChanges(): Flow<Unit> = callbackFlow {
    val resolver = appContext.contentResolver
    val uri = if (isQPlus()) MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        else MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
    val observer = object : ContentObserver(null) {
        override fun onChange(selfChange: Boolean) { trySend(Unit) }
    }
    resolver.registerContentObserver(uri, true, observer)
    awaitClose { resolver.unregisterContentObserver(observer) }
}.buffer(Channel.CONFLATED)

object MediaStoreAudioCatalogObserver : AudioCatalogObserver {
    override fun observe(scope: CoroutineScope): Job = scope.launch {
        try {
            forwardAudioCatalogChanges(audioCatalogNotifications()) {
                RustHostReports.mediaCatalogChanged(JsonHelper.jsonEncode(MediaCatalogChange(listOf(DataType.AUDIO.value))))
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            LogCat.e("Audio catalog observation", error)
        }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
private fun audioCatalogNotifications(): Flow<Unit> = merge(
    flowOf(Unit),
    AppChannel.sharedFlow.filterIsInstance<PermissionsResultEvent>()
        .filter { it.has(Permission.READ_MEDIA_AUDIO) || it.has(Permission.WRITE_EXTERNAL_STORAGE) }
        .map { Unit },
).flatMapLatest {
    audioCatalogChanges().catch { error -> LogCat.e("Audio catalog observer registration", error) }
}
