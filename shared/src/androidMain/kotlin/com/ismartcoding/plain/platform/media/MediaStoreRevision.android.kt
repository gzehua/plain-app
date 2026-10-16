package com.ismartcoding.plain.platform.media

import android.provider.MediaStore
import com.ismartcoding.plain.appContext
import com.ismartcoding.plain.platform.isRPlus

internal actual fun mediaStoreRevision(): List<MediaVolumeRevision>? {
    if (!isRPlus()) return null
    return MediaStore.getExternalVolumeNames(appContext).sorted().map { volume ->
        MediaVolumeRevision(volume, MediaStore.getVersion(appContext, volume), MediaStore.getGeneration(appContext, volume))
    }
}
