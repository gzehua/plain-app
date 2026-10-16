package com.ismartcoding.plain.platform.media

import android.database.ContentObserver
import android.provider.MediaStore
import com.ismartcoding.plain.appContext
import com.ismartcoding.plain.platform.isQPlus
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow

internal actual fun audioCatalogChanges(): Flow<Unit> = callbackFlow {
    val resolver = appContext.contentResolver
    val uri = if (isQPlus()) MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        else MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
    val observer = object : ContentObserver(null) {
        override fun onChange(selfChange: Boolean) { trySend(Unit) }
    }
    resolver.registerContentObserver(uri, true, observer)
    awaitClose { resolver.unregisterContentObserver(observer) }
}.buffer(Channel.CONFLATED)
