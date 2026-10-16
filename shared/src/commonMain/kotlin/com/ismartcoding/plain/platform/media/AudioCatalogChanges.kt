package com.ismartcoding.plain.platform.media

import kotlinx.coroutines.flow.Flow

internal expect fun audioCatalogChanges(): Flow<Unit>
