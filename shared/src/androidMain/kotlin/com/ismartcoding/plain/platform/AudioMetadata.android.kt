package com.ismartcoding.plain.platform

actual fun audioAlbumArtUri(albumId: String): String = android.net.Uri.withAppendedPath(
    android.net.Uri.parse("content://media/external/audio/albumart"), albumId,
).toString()
