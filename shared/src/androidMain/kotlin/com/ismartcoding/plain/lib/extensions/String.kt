package com.ismartcoding.plain.lib.extensions

import android.net.Uri
import android.provider.MediaStore
import android.telephony.PhoneNumberUtils
import com.ismartcoding.plain.platform.isQPlus
import java.io.File

fun String.pathToUri(): Uri {
    if (startsWith("/")) {
        return Uri.fromFile(File(this))
    }

    return Uri.parse(this)
}

fun String.normalizePhoneNumber(): String = PhoneNumberUtils.normalizeNumber(this)

fun String.pathToMediaStoreBaseUri(): Uri {
    return when {
        isImageFast() -> if (isQPlus()) MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL) else MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        isVideoFast() -> if (isQPlus()) MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL) else MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        isAudioFast() -> if (isQPlus()) MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL) else MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        else -> MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
    }
}

fun String.pathToMediaStoreUri(id: String): Uri {
    return Uri.withAppendedPath(pathToMediaStoreBaseUri(), id)
}
