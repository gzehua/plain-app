package com.ismartcoding.plain.platform.media

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

internal actual fun audioCatalogChanges(): Flow<Unit> = emptyFlow()
