package com.ismartcoding.plain.platform.media

import android.net.Uri

fun audioAlbumArtUri(albumId: String): String = Uri.withAppendedPath(
    Uri.parse("content://media/external/audio/albumart"), albumId,
).toString()
