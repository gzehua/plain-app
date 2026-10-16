package com.ismartcoding.plain.platform.media

import android.database.ContentObserver
import android.provider.MediaStore
import com.ismartcoding.plain.appContext
import com.ismartcoding.plain.platform.isQPlus

object MediaStoreImageObserver {
    private var observer: ContentObserver? = null
    @Synchronized
    fun startup() {
        if (observer != null) return
        val uri = if (isQPlus()) MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL) else MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val next = object : ContentObserver(null) {
            override fun onChange(selfChange: Boolean) { reportImageCatalogChanged() }
        }
        appContext.contentResolver.registerContentObserver(uri, true, next)
        observer = next
    }
    @Synchronized
    fun shutdown() {
        observer?.let { appContext.contentResolver.unregisterContentObserver(it) }
        observer = null
        MediaStoreImageCatalog.close()
    }
}
