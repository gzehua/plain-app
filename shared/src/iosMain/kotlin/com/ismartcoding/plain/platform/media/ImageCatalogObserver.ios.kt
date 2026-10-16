package com.ismartcoding.plain.platform.media

internal actual suspend fun observeImageCatalog(enabled: Boolean) = IosPhotoLibrary.observe(enabled)
