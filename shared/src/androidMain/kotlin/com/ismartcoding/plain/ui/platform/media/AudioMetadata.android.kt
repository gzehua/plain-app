package com.ismartcoding.plain.ui.platform.media

import android.net.Uri

actual fun audioAlbumArtUri(albumId: String): String = Uri.withAppendedPath(
    Uri.parse("content://media/external/audio/albumart"), albumId,
).toString()
