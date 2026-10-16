package com.ismartcoding.plain.features.images.search

import com.ismartcoding.plain.platform.system.IosImageIndexCatalog

internal actual suspend fun observeImageModels(enabled: Boolean) = IosImageIndexCatalog.observe(enabled)
