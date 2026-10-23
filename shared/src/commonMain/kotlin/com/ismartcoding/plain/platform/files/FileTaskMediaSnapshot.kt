package com.ismartcoding.plain.platform.files

import com.ismartcoding.plain.platform.media.MediaStoreFileRow

expect fun queryFileTaskMedia(paths: List<String>): List<MediaStoreFileRow>
