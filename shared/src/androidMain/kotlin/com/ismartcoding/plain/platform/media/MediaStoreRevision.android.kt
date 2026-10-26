package com.ismartcoding.plain.platform.media

import android.provider.MediaStore
import kotlinx.serialization.Serializable
import com.ismartcoding.plain.appContext
import com.ismartcoding.plain.platform.isRPlus

internal fun mediaStoreRevision(): List<MediaVolumeRevision>? {
    if (!isRPlus()) return null
    return MediaStore.getExternalVolumeNames(appContext).sorted().map { volume ->
        MediaVolumeRevision(volume, MediaStore.getVersion(appContext, volume), MediaStore.getGeneration(appContext, volume))
    }
}

@Serializable
internal data class MediaVolumeRevision(val volume: String, val version: String, val generation: Long)
